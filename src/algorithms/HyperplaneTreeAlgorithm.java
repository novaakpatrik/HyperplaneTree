package algorithms;

import messif.algorithms.Algorithm;
import messif.buckets.Bucket;
import messif.buckets.BucketDispatcher;
import messif.buckets.BucketStorageException;
import messif.buckets.impl.MemoryStorageBucket;
import messif.objects.LocalAbstractObject;
import messif.operations.data.BulkInsertOperation;
import messif.operations.data.InsertOperation;
import messif.operations.query.KNNQueryOperation;
import messif.operations.query.RangeQueryOperation;
import messif.pivotselection.AbstractPivotChooser;
import nodes.HyperplaneTreeNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HyperplaneTreeAlgorithm extends Algorithm {
    /**
     * Class ID for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Bucket dispatcher for managing buckets in this algorithm
     */
    private final BucketDispatcher bucketDispatcher;
    /**
     * Root node of the Generic Hyperplane Tree
     */
    private HyperplaneTreeNode root;

    /**
     * Creates a new instance of Generic Hyperplane Tree algorithm.
     *
     * @throws BucketStorageException if the maximal number of buckets is already allocated
     */
    @AlgorithmConstructor(
            description = "Create empty Hyperplane Tree node",
            arguments = {"pivotChooser", "nodeClass", "bucketCapacity"})
    public HyperplaneTreeAlgorithm(
            AbstractPivotChooser pivotChooser,
            Class<? extends HyperplaneTreeNode> nodeClass,
            long bucketCapacity) throws BucketStorageException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super("Hyperplane Tree of type: " + nodeClass.getSimpleName());
        // Set default bucket capacity and class
        bucketDispatcher = new BucketDispatcher(Integer.MAX_VALUE, bucketCapacity, MemoryStorageBucket.class);
        // Set default pivot chooser
        bucketDispatcher.setAutoPivotChooser(pivotChooser);
        // Create new root node (leaf) */
        Constructor constructor = nodeClass.getDeclaredConstructor(Bucket.class);
        root = (HyperplaneTreeNode) constructor.newInstance(bucketDispatcher.createBucket());
    }

    /**
     * Creates a new instance of Generic Hyperplane Tree algorithm.
     *
     * @throws BucketStorageException if the maximal number of buckets is already allocated
     */
    @AlgorithmConstructor(
            description = "Create Hyperplane Tree node with inserted objects and propagate" +
                            " them to child nodes based on the specified capacity",
            arguments = {"pivotChooser", "nodeClass", "desiredBucketCapacity", "insertedObjects"})
    public HyperplaneTreeAlgorithm(
            AbstractPivotChooser pivotChooser,
            Class<? extends HyperplaneTreeNode> nodeClass,
            long desiredBucketCapacity,
            List<? extends LocalAbstractObject> insertedObjects) throws BucketStorageException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super("Hyperplane Tree of type: " + nodeClass.getSimpleName());
        // Set initial bucket capacity and class
        bucketDispatcher = new BucketDispatcher(Integer.MAX_VALUE, insertedObjects.size(), MemoryStorageBucket.class);
        // Set default pivot chooser
        bucketDispatcher.setAutoPivotChooser(pivotChooser);
        //Create bucket with inserted objects
        Bucket bucket = bucketDispatcher.createBucket();
        bucket.addObjects(insertedObjects);
        // Create new root node (leaf) */
        Constructor constructor = nodeClass.getDeclaredConstructor(Bucket.class);
        root = (HyperplaneTreeNode) constructor.newInstance(bucket);
        //Propagate the capacity change to the tree
        root.propagateBucketCapacity(bucketDispatcher, desiredBucketCapacity);
    }

    /**
     * Executes insert operation.
     *
     * @param operation the operation with the object to insert
     * @throws BucketStorageException if there was an error storing the object
     */
    public void processInsert(InsertOperation operation) throws BucketStorageException {
        root.insert(operation.getInsertedObject(), bucketDispatcher);
        operation.endOperation();
    }

    /**
     * Executes bulk insert operation.
     *
     * @param operation the operation with list of objects to insert
     * @throws BucketStorageException if there was an error storing the object
     */
    public void processBulkInsert(BulkInsertOperation operation) throws BucketStorageException {
        for (LocalAbstractObject obj : operation.getInsertedObjects())
            root.insert(obj, bucketDispatcher);
        operation.endOperation();
    }

    /**
     * Executes range search on the structure.
     *
     * @param operation the operation with query object and radius
     */
    public void processRangeSearch(RangeQueryOperation operation) {
        root.rangeSearch(operation);
        operation.endOperation();
    }

    /**
     * Executes KNN search on the structure.
     *
     * @param operation the operation with query object and radius
     */
    public void processNearestNeighborSearch(KNNQueryOperation operation) {
        root.nearestNeighborSearch(operation);
        operation.endOperation();
    }

    @Override
    public String toString() {
        return "Algorithm: " + getName() + "\r\n" +
                "Objects: " + bucketDispatcher.getObjectCount() + "\r\n";
    }

}
