public class Bucket {

    private int min
    private int max
    private int size

    public Bucket(int min, int max, int size) {
        this.min = min
        this.max = max
        this.size = size
    }

    public Bucket(int min, int max) {
        this(min, max, 0)
    }

    public void add() {
        size++
    }

    public static List<Bucket> fixedBucketDivide(List<Integer> inputs, int numBuckets) {
        final int min = inputs.min()
        final int range = Stats.range(inputs)
        final int totalNumbers = range + 1
        final int bucketSize = totalNumbers/numBuckets // integer division is exactly what we want
        final int oversize = bucketSize + 1
        final int numOversizeBuckets = totalNumbers % numBuckets // this number of buckets need to have 1 extra length in length
        final int minInStandardBucket = min + numOversizeBuckets * oversize
        final List<Bucket> buckets = new ArrayList<>();

        for (int i = 0; i < numOversizeBuckets; i++) {
            buckets.add(new Bucket(min + i * oversize, min + i * oversize + bucketSize))
        }
        
        for (int i = 0; i < numBuckets - numOversizeBuckets; i++) {
            buckets.add(new Bucket(minInStandardBucket + i * bucketSize, minInStandardBucket + i * bucketSize + bucketSize - 1))
        }


        for (int i in inputs) {
            int index
            if (i <= min + numOversizeBuckets * oversize - 1) { // if it's in an oversized bucket...
                index = (i - min) / oversize
            } else {
                index = numOversizeBuckets + ((i - minInStandardBucket) / bucketSize)
            }
            buckets.get(index).add()
        }

        return buckets
    }

    public static String join(List<Bucket> buckets) {
        return buckets.join(', ')
    }

    public String toString() {
        return "${min}-${max}"
    }

}
