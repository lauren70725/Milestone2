package milestone2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheSimulatorMilestone2 {
    private static Cache cache; // Global cache instance

    public static void main(String[] args) {
        if (args.length != 12) {
            System.out.println("Invalid number of arguments. ");
            System.out.println("Usage: Sim.exe -f <trace file name> -s <cache size in KB> -b <block size> -a <associativity> -r <replacement policy> -p <physical memory in KB>");
            return;
        }

        String traceFileName = "";
        int cacheSize = 0;
        int blockSize = 0;
        int associativity = 0;
        String replacementPolicy = "";
        int physicalMemory = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f":
                    if (i + 1 < args.length) {
                        traceFileName = args[i + 1];
                        i++; // Move to the next argument
                    }
                    break;
                case "-s":
                    if (i + 1 < args.length) {
                        cacheSize = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                case "-b":
                    if (i + 1 < args.length) {
                        blockSize = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                case "-a":
                    if (i + 1 < args.length) {
                        associativity = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                case "-r":
                    if (i + 1 < args.length) {
                        replacementPolicy = args[i + 1];
                        i++;
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        physicalMemory = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                default:
                    // Handle unexpected or incorrect input
                    System.out.println("Invalid arguments provided.");
                    return;
            }
        }

        // Display the input parameters and calculated values
        System.out.println("Cache Simulator CS 3853 - Group #10\n");
        System.out.println("Trace File: " + traceFileName + "\n");
        System.out.println("***** Cache Input Parameters *****");
        System.out.println("Cache Size: " + cacheSize + " KB");
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Associativity: " + associativity);
        System.out.println("Replacement Policy: " + replacementPolicy + "\n");

        // Calculating values based on provided formulas
        int totalBlocks = cacheSize * 1024 / blockSize;
        int tagSize = 32 - (int) (Math.log(blockSize) / Math.log(2)) - (int) (Math.log(totalBlocks / associativity) / Math.log(2));
        int indexSize = (int) (Math.log(totalBlocks / associativity) / Math.log(2));
        int totalRows = totalBlocks / associativity;
        int overheadSize = totalRows * (3 + tagSize + (blockSize * 8)); // Assuming 8 bits per byte
        double implementationMemorySize = ((overheadSize + cacheSize * 1024) / 1024.0); // In KB
        double cost = implementationMemorySize * 0.09; // $0.09 per KB

        System.out.println("***** Cache Calculated Values *****\n");
        System.out.println("Total # Blocks: " + totalBlocks);
        System.out.println("Tag Size: " + tagSize + " bits");
        System.out.println("Index Size: " + indexSize + " bits");
        System.out.println("Total # Rows: " + totalRows);
        System.out.println("Overhead Size: " + overheadSize + " bytes");
        System.out.println("Implementation Memory Size: " + String.format("%.2f", implementationMemorySize) + " KB");
        System.out.println("Cost: $" + String.format("%.2f", cost) + " @ ($0.09 / KB)\n");

        // Initialize the cache with your desired parameters
        cache = new Cache(cacheSize * 1024, blockSize, associativity);

        // Read and parse the memory trace file
        try (BufferedReader br = new BufferedReader(new FileReader(traceFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Parse the line from the trace file
                MemoryAccess memoryAccess = parseTraceFile(line);

                // Now you can use the information in 'memoryAccess' to simulate cache behavior
                if (memoryAccess != null) {
                    simulateCache(memoryAccess, blockSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate derived metrics
        int totalCacheAccesses = cache.getTotalCacheAccesses();
        int cacheHits = 0; // calculate cacheHits based on your simulation
        int compulsoryMisses = 0; // calculate compulsoryMisses based on your simulation

        int cacheMisses = totalCacheAccesses - cacheHits;
        double hitRate = (double) cacheHits / totalCacheAccesses * 100;
        double missRate = 100 - hitRate;
        double cpi = 4.13;

        // Print simulation results
        System.out.println("***** CACHE SIMULATION RESULTS *****");
        System.out.println("Total Cache Accesses: " + totalCacheAccesses);
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("--- Compulsory Misses: " + compulsoryMisses);
        // ... (print other metrics)

        // Calculate unused cache space and waste
        double unusedKB = Math.max(((totalBlocks - compulsoryMisses) * (blockSize + overheadSize)) / 1024.0, 0);
        double waste = Math.max(cost / 1024.0 * unusedKB, 0);

        System.out.println("Unused Cache Space: " + String.format("%.2f", unusedKB) + " KB / " + (cacheSize * 1024) + " KB = " + String.format("%.2f", unusedKB / (cacheSize * 1024) * 100) + "%");
        System.out.println("Waste: $" + String.format("%.2f", waste));
        System.out.println("Unused Cache Blocks: " + (cacheSize * 1024 - totalBlocks + compulsoryMisses) + " / " + (cacheSize * 1024) + "\n");
    }

    // MemoryAccess class to represent a memory access
    static class MemoryAccess {
        private int address;
        private int length;
        private boolean isInstruction;

        public MemoryAccess(int address, int length, boolean isInstruction) {
            this.address = address;
            this.length = length;
            this.isInstruction = isInstruction;
        }

        public int getAddress() {
            return address;
        }

        public int getLength() {
            return length;
        }

        public boolean isInstruction() {
            return isInstruction;
        }
    }

    // Cache class to represent the cache and simulate cache behavior
    static class Cache {
        private int size; // Cache size in bytes
        private int blockSize; // Block size in bytes
        private int associativity; // Cache associativity
        private Map<Integer, CacheLine> cacheLines;
        private int totalCacheAccesses;

        public Cache(int size, int blockSize, int associativity) {
            this.size = size;
            this.blockSize = blockSize;
            this.associativity = associativity;
            this.cacheLines = new HashMap<>();
            this.totalCacheAccesses = 0;
        }

        public int getTotalCacheAccesses() {
            return totalCacheAccesses;
        }
    }

    // CacheLine class to represent a cache line and simulate cache line behavior
    static class CacheLine {
        private int associativity;
        private Map<Integer, Integer> tags; // Tag -> Last accessed block number

        public CacheLine(int associativity) {
            this.associativity = associativity;
            this.tags = new HashMap<>();
        }

        public int simulateAccess(int tag, int blockNumber, int length) {
            // Existing logic
            if (tags.containsKey(tag)) {
                // Cache hit
                int lastAccessedBlock = tags.get(tag);
                System.out.println("Cache Hit - Tag: " + tag + ", Block: " + lastAccessedBlock);
            } else {
                // Cache miss
                System.out.println("Cache Miss - Tag: " + tag + ", Block: " + blockNumber);

                // Update cache line
                tags.put(tag, blockNumber);
            }

            // Increment totalCacheAccesses
            cache.totalCacheAccesses++;

            // Return the number of cycles for this access
            return length / 4; // Assuming 1 cycle for each 4 bytes accessed (adjust as needed)
        }
    }

    // Placeholder method for parsing the trace file
    private static MemoryAccess parseTraceFile(String line) {
        // Check if the line contains the expected parts
        if (line.startsWith("EIP (")) {
            // Instruction line
            return parseInstructionLine(line);
        } else if (line.startsWith("dstM")) {
            // dstM line
            return parseDstMLine(line);
        } else if (line.startsWith("srcM")) {
            // srcM line
            return parseSrcMLine(line);
        }

        // Invalid line format
        return null;
    }

    // Modify the parseInstructionLine method
    private static MemoryAccess parseInstructionLine(String line) {
        // Extract length, address, and ignore the rest
        String[] parts = line.split(":");
        if (parts.length < 2) {
            // Invalid line format
            return null;
        }

        String[] addressInfo = parts[1].trim().split("\\s+");
        if (addressInfo.length < 2) {
            // Invalid address information
            return null;
        }

        String addressStr = addressInfo[1];

        try {
            // Assuming instructions have "EIP" in the line
            return new MemoryAccess(Integer.parseInt(addressStr, 16), 0, true);
        } catch (NumberFormatException e) {
            // Print the problematic line and the exception message
            System.out.println("Error parsing line: " + line);
            System.out.println("Exception message: " + e.getMessage());
            return null;
        }
    }

    // Modify the parseDstMLine method
    private static MemoryAccess parseDstMLine(String line) {
        // Process dstM line if needed
        // For now, let's just return null
        return null;
    }

    // Modify the parseSrcMLine method
    private static MemoryAccess parseSrcMLine(String line) {
        // Process srcM line if needed
        // For now, let's just return null
        return null;
    }

    // Move the simulateCache method here, outside the Cache class
    private static void simulateCache(MemoryAccess memoryAccess, int blockSize) {
        // Cache hit or miss logic
        int address = memoryAccess.getAddress();
        int length = memoryAccess.getLength();

        // Assume 4 bytes can be accessed simultaneously (32-bit data bus)
        int bytesAccessedSimultaneously = 4;
        int blocksToRead = (int) Math.ceil((double) length / bytesAccessedSimultaneously);

        // Increment totalCacheAccesses only once for each memory access
        cache.totalCacheAccesses++;

        for (int i = 0; i < blocksToRead; i++) {
            // Calculate block number and set index
            int blockNumber = (address + i * bytesAccessedSimultaneously) / blockSize;
            int setIndex = blockNumber % (cache.size / (blockSize * cache.associativity));
            int tag = blockNumber / (cache.size / (blockSize * cache.associativity));

            CacheLine cacheLine = cache.cacheLines.computeIfAbsent(setIndex, k -> new CacheLine(cache.associativity));
            cacheLine.simulateAccess(tag, blockNumber, length);
        }
    }

}
