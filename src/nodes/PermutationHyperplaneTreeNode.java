package nodes;

import messif.buckets.Bucket;
import messif.pivotselection.AbstractPivotChooser;

import java.util.List;

/**
 * Class representing permutation hyperplane tree node.
 * <p>
 * Reference points of a node are selected based on permutation strategy.
 * Each node has set permutation indeces passed/set on it Permutation indices of next reference points are passed from parent to child nodes
 * When all possible permutations of points have been used, new point is added and permutations with
 * this node
 */
public class PermutationHyperplaneTreeNode extends HyperplaneTreeNode {

    private int permutationPivotIndex;
    private int lastPivotIndex;

    public PermutationHyperplaneTreeNode(Bucket bucket) {
        super(bucket);
        permutationPivotIndex = 0;
        lastPivotIndex = 1;
    }

    private PermutationHyperplaneTreeNode(Bucket bucket, int permutationPivotIndex, int lastPivotIndex) {
        super(bucket);
        this.permutationPivotIndex = permutationPivotIndex;
        this.lastPivotIndex = lastPivotIndex;
    }

    @Override
    void setUpReferencePoints(AbstractPivotChooser pivotChooser) {

        if (permutationsExceeded()) {
            permutationPivotIndex -= lastPivotIndex;
            lastPivotIndex++;
        }

        leftReferencePoint = pivotChooser.getPivot(lastPivotIndex);
        rightReferencePoint = pivotChooser.getPivot(permutationPivotIndex);
    }

    private boolean permutationsExceeded() {
        return permutationPivotIndex >= lastPivotIndex;
    }

    @Override
    void setUpChildNodes(List<Bucket> buckets) {
        leftChild = new PermutationHyperplaneTreeNode(
                buckets.get(LEFT_BUCKET_INDEX),
                permutationPivotIndex + 1,
                lastPivotIndex);
        rightChild = new PermutationHyperplaneTreeNode(buckets.get(
                RIGHT_BUCKET_INDEX),
                permutationPivotIndex + 2,
                lastPivotIndex);
    }
}
