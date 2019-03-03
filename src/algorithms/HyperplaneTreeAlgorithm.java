package algorithms;

import messif.algorithms.Algorithm;
import messif.buckets.Bucket;
import messif.buckets.BucketDispatcher;
import messif.buckets.BucketStorageException;
import messif.buckets.impl.MemoryStorageBucket;
import messif.buckets.split.SplitPolicy;
import messif.objects.LocalAbstractObject;
import messif.operations.data.BulkInsertOperation;
import messif.operations.data.InsertOperation;
import messif.operations.query.RangeQueryOperation;
import messif.pivotselection.AbstractPivotChooser;
import nodes.HyperplaneTreeNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    @AlgorithmConstructor(description = "Create empty Hyperplane Tree node", arguments = {"nodeType", "pivotChooser", "splitPolicy"})
    public HyperplaneTreeAlgorithm(
            AbstractPivotChooser pivotChooser,
            Class<? extends HyperplaneTreeNode> nodeClass) throws BucketStorageException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super("Hyperplane Tree of type: " + nodeClass.getSimpleName());
        // Set default bucket capacity and class
        bucketDispatcher = new BucketDispatcher(Integer.MAX_VALUE, 10000, MemoryStorageBucket.class);
        // Set default pivot chooser
        bucketDispatcher.setAutoPivotChooser(pivotChooser);
        // Create new root node (leaf) */
        Constructor constructor = nodeClass.getDeclaredConstructor(Bucket.class);
        root = (HyperplaneTreeNode) constructor.newInstance(bucketDispatcher.createBucket());
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

    @Override
    public String toString() {
        return "Algorithm: " + getName() + "\r\n" +
                "Objects: " + bucketDispatcher.getObjectCount() + "\r\n";
    }

}
