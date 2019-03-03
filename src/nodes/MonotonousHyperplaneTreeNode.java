package nodes;

import messif.buckets.Bucket;
import messif.objects.LocalAbstractObject;
import messif.pivotselection.AbstractPivotChooser;

import java.util.List;

/**
 * Class representing monotonous hyperplane tree node.
 * <p>
 * Each node shares a reference point with its parent.
 * <p>
 * Each inner node shares
 */
public class MonotonousHyperplaneTreeNode extends HyperplaneTreeNode {

    public MonotonousHyperplaneTreeNode(Bucket bucket) {
        super(bucket);
    }

    private MonotonousHyperplaneTreeNode(Bucket bucket, LocalAbstractObject sharedReferencePoint) {
        super(bucket);
        this.leftReferencePoint = sharedReferencePoint;
    }

    @Override
    void setUpReferencePoints(AbstractPivotChooser pivotChooser) {

        if (leftReferencePoint == null) {
            leftReferencePoint = pivotChooser.getPivot();
        }

        rightReferencePoint = pivotChooser.getPivot();
        pivotChooser.clear();
    }

    @Override
    void setUpChildNodes(List<Bucket> buckets) {
        leftChild = new MonotonousHyperplaneTreeNode(buckets.get(LEFT_BUCKET_INDEX), leftReferencePoint);
        rightChild = new MonotonousHyperplaneTreeNode(buckets.get(RIGHT_BUCKET_INDEX), rightReferencePoint);
    }
}
