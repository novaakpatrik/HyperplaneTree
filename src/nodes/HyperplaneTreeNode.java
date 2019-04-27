package nodes;

import messif.buckets.Bucket;
import messif.buckets.BucketDispatcher;
import messif.buckets.BucketStorageException;
import messif.buckets.CapacityFullException;
import messif.buckets.split.impl.SplitPolicyGeneralizedHyperplane;
import messif.objects.LocalAbstractObject;
import messif.operations.query.KNNQueryOperation;
import messif.operations.query.RangeQueryOperation;
import messif.pivotselection.AbstractPivotChooser;

import java.util.ArrayList;
import java.util.List;

public abstract class HyperplaneTreeNode {

    static final int LEFT_BUCKET_INDEX = 0;
    static final int RIGHT_BUCKET_INDEX = 1;

    LocalAbstractObject leftReferencePoint;
    LocalAbstractObject rightReferencePoint;
    HyperplaneTreeNode leftChild;
    HyperplaneTreeNode rightChild;

    private Bucket bucket;

    HyperplaneTreeNode(Bucket bucket) {
        this.bucket = bucket;
    }

    public void insert(LocalAbstractObject obj, BucketDispatcher bucketDispatcher) throws BucketStorageException {
        if (bucket != null) {
            try {
                bucket.addObject(obj);
                return;
            } catch (CapacityFullException e) {
                split(bucketDispatcher);
            }
        }

        if (leftReferencePoint.getDistance(obj) <= rightReferencePoint.getDistance(obj)) {
            leftChild.insert(obj, bucketDispatcher);
        } else {
            rightChild.insert(obj, bucketDispatcher);
        }
    }

    private void split(BucketDispatcher bucketDispatcher) throws BucketStorageException {
        AbstractPivotChooser pivotChooser = bucketDispatcher.getAutoPivotChooser(bucket.getBucketID());
        setUpReferencePoints(pivotChooser);

        List<Bucket> buckets = splitIntoBuckets(bucketDispatcher);
        setUpChildNodes(buckets);

        cleanUpBucket(bucketDispatcher);
    }

    /**
     * Sets up reference points for this node, which are used to divide data between its child nodes.
     *
     * @param pivotChooser pivotChooser
     */
    abstract void setUpReferencePoints(AbstractPivotChooser pivotChooser);

    private List<Bucket> splitIntoBuckets(BucketDispatcher bucketDispatcher) throws BucketStorageException {
        List<Bucket> buckets = new ArrayList<>();
        SplitPolicyGeneralizedHyperplane splitPolicy = setUpSplitPolicy();

        bucket.split(splitPolicy, buckets, bucketDispatcher, RIGHT_BUCKET_INDEX);
        buckets.add(bucket);
        return buckets;
    }

    private SplitPolicyGeneralizedHyperplane setUpSplitPolicy() {
        SplitPolicyGeneralizedHyperplane splitPolicy = new SplitPolicyGeneralizedHyperplane();
        splitPolicy.setLeftPivot(leftReferencePoint);
        splitPolicy.setRightPivot(rightReferencePoint);
        return splitPolicy;
    }

    abstract void setUpChildNodes(List<Bucket> buckets) throws BucketStorageException;

    private void cleanUpBucket(BucketDispatcher bucketDispatcher) {
        bucketDispatcher.removeBucket(bucket.getBucketID(), true);
        bucket = null;
    }

    public void rangeSearch(RangeQueryOperation operation) {
        if (bucket != null) {
            bucket.processQuery(operation);
            return;
        }

        LocalAbstractObject queryObject = operation.getQueryObject();
        float leftPivotDistance = queryObject.getDistance(leftReferencePoint);
        float rightPivotDistance = queryObject.getDistance(rightReferencePoint);
        float radius = operation.getRadius();

        //pivot exclusion
        if (leftPivotDistance <= radius)
            operation.addToAnswer(leftReferencePoint);
        if (rightPivotDistance <= radius)
            operation.addToAnswer(rightReferencePoint);
        //hyperplane exclusion
        if (leftPivotDistance - radius <= rightPivotDistance + radius)
            leftChild.rangeSearch(operation);
        if (leftPivotDistance + radius >= rightPivotDistance - radius)
            rightChild.rangeSearch(operation);
    }

    public void nearestNeighborSearch(KNNQueryOperation operation) {
        if (bucket != null) {
            bucket.processQuery(operation);
            return;
        }

        LocalAbstractObject queryObject = operation.getQueryObject();
        float leftPivotDistance = queryObject.getDistance(leftReferencePoint);
        float rightPivotDistance = queryObject.getDistance(rightReferencePoint);
        float answerDistance = operation.getAnswerDistance();

        //pivot exclusion
        if (leftPivotDistance <= answerDistance)
            operation.addToAnswer(leftReferencePoint);
        if (rightPivotDistance <= answerDistance)
            operation.addToAnswer(rightReferencePoint);
        //hyperplane exclusion
        if (leftPivotDistance - answerDistance <= rightPivotDistance + answerDistance)
            leftChild.nearestNeighborSearch(operation);
        if (leftPivotDistance + answerDistance >= rightPivotDistance - answerDistance)
            rightChild.nearestNeighborSearch(operation);
    }

    public void propagateBucketCapacity(BucketDispatcher bucketDispatcher, long desiredBucketCapacity) throws BucketStorageException {

        long currentCapacity = bucketDispatcher.getBucketCapacity();

        if (currentCapacity <= desiredBucketCapacity)
            return;

        long newCapacity = currentCapacity / 2;

        if (newCapacity < desiredBucketCapacity)
            newCapacity = desiredBucketCapacity;

        bucketDispatcher.setBucketCapacity(newCapacity);
        split(bucketDispatcher);

        leftChild.propagateBucketCapacity(bucketDispatcher, desiredBucketCapacity);
        rightChild.propagateBucketCapacity(bucketDispatcher, desiredBucketCapacity);
    }
}
