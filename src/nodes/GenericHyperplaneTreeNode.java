package nodes;

import messif.buckets.Bucket;
import messif.pivotselection.AbstractPivotChooser;

import java.util.List;

/**
 * Class representing generic hyperplane tree node.
 */
public class GenericHyperplaneTreeNode extends HyperplaneTreeNode {

    private GenericHyperplaneTreeNode(Bucket bucket) {
        super(bucket);
    }

    @Override
    void setUpReferencePoints(AbstractPivotChooser pivotChooser) {
        leftReferencePoint = pivotChooser.getPivot();
        rightReferencePoint = pivotChooser.getPivot();
    }

    @Override
    void setUpChildNodes(List<Bucket> buckets) {
        leftChild = new GenericHyperplaneTreeNode(buckets.get(LEFT_BUCKET_INDEX));
        rightChild = new GenericHyperplaneTreeNode(buckets.get(RIGHT_BUCKET_INDEX));
    }
}
