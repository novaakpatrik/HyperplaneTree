package nodes;

import messif.buckets.Bucket;
import messif.pivotselection.AbstractPivotChooser;

import java.util.List;

/**
 * Class representing leanest hyperplane tree node.
 * <p>
 * Reference points of a node are selected based on its depth in a tree.
 * For a node with depth n the indices of its reference points are 2 * n and 2 * n + 1.
 */
public class LeanestHyperplaneTreeNode extends HyperplaneTreeNode {

    private int depth;

    public LeanestHyperplaneTreeNode(Bucket bucket) {
        super(bucket);
        this.depth = 0;
    }

    private LeanestHyperplaneTreeNode(Bucket bucket, int depth) {
        super(bucket);
        this.depth = depth;
    }

    @Override
    void setUpReferencePoints(AbstractPivotChooser pivotChooser) {
        leftReferencePoint = pivotChooser.getPivot(2 * depth);
        rightReferencePoint = pivotChooser.getPivot(2 * depth + 1);
    }

    @Override
    void setUpChildNodes(List<Bucket> buckets) {
        leftChild = new LeanestHyperplaneTreeNode(buckets.get(LEFT_BUCKET_INDEX), depth + 1);
        rightChild = new LeanestHyperplaneTreeNode(buckets.get(RIGHT_BUCKET_INDEX), depth + 1);
    }
}
