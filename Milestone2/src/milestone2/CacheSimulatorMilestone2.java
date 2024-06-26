package milestone2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheSimulatorMilestone2 {
    private static Cache cache;

    public static void main(String[] args) {
        if (args.length != 12) {
            System.out.println("Invalid number of arguments. ");
            System.out.println("Usage: sim.exe –s <cache size in KB> –b <block size> –a <associativity> –r <replacement policy> –p <physical memory - MB> -u <% phys mem used> -n <Instr / Time Slice> –f <trace file name>");
            return;
        }

        String traceFileName = "";
        int cacheSize = 0;
        int blockSize = 0;
        int associativity = 0;
        String replacementPolicy = "";
        int physicalMemorySize = 0; 
        int percentMemoryUsed = 0;
        int instructionsPerTimeSlice = 0;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
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
                        physicalMemorySize = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                case "-n":
                    if (i + 1 < args.length) {
                        instructionsPerTimeSlice = Integer.parseInt(args[i + 1]);
                        i++;
                    }
                    break;
                case "-u":
                    if (i + 1 < args.length) {
                        percentMemoryUsed = Integer.parseInt(args[i + 1]);
                        i++;
                    break;
                    }
                case "-f":
                    if (i + 1 < args.length) {
                    	traceFileName = args[i + 1];
                        i++;
                    }
                    break;
                default:
                    // Handle unexpected or incorrect input
                	System.out.println("Unexpected argument: " + args[i]);
                    return;
         }
       }
  
        
        // Display the input parameters and calculated values
        System.out.println("Cache Simulator CS 3853 - Group #10\n");
        System.out.println("Trace File: " + traceFileName + "\n");
        System.out.println("***** Input Parameters *****");
        System.out.println("Cache Size: " + cacheSize + " KB");
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Associativity: " + associativity);
        System.out.println("Replacement Policy: " + replacementPolicy);
        System.out.println("Physical Memory: " + physicalMemorySize + " MB");
        System.out.println("Percent Memory Used by System: " + percentMemoryUsed);
        System.out.println("Instructions / Time Slice: " + instructionsPerTimeSlice + "\n");

        // Calculating values based on provided formulas
        int totalBlocks = cacheSize * 1024 / blockSize;
        int tagSize = 32 - (int) (Math.log(blockSize) / Math.log(2)) - (int) (Math.log(totalBlocks / associativity) / Math.log(2));
        int indexSize = (int) (Math.log(totalBlocks / associativity) / Math.log(2));
        int totalRows = totalBlocks / associativity;
        int overheadSize = totalRows * (3 + tagSize + (blockSize * 8)); 
        double implementationMemorySize = ((overheadSize + cacheSize * 1024) / 1024.0); 
        double cost = implementationMemorySize * 0.09; 

        int numPhysicalPages = physicalMemorySize * 256; 
        int numPagesForSystem = (int)(numPhysicalPages * (percentMemoryUsed / 100.0));
        int sizeOfPageTableEntry = 20; 
        long totalRAMForPageTables = (long) numPhysicalPages * sizeOfPageTableEntry / 8; // Bytes
        
        System.out.println("\n***** Cache Calculated Values *****");
        System.out.println("Total # Blocks: " + totalBlocks);
        System.out.println("Tag Size: " + tagSize + " bits");
        System.out.println("Index Size: " + indexSize + " bits");
        System.out.println("Total # Rows: " + totalRows);
        System.out.println("Overhead Size: " + overheadSize + " bytes");
        System.out.println(String.format("Implementation Memory Size: %.2f KB (%d bytes)", implementationMemorySize, (int)(implementationMemorySize * 1024)));
        System.out.println(String.format("Cost: $%.2f @ $0.15 / KB", cost));
        
        System.out.println("\n***** Physical Memory Calculated Values *****");
        System.out.println("Number of Physical Pages: " + numPhysicalPages);
        System.out.println("Number of Pages for System: " + numPagesForSystem);
        System.out.println("Size of Page Table Entry: " + sizeOfPageTableEntry + " bits");
        System.out.println("Total RAM for Page Table(s): " + totalRAMForPageTables + " bytes");
        
        cache = new Cache(cacheSize * 1024, blockSize, associativity);

       
        try (BufferedReader br = new BufferedReader(new FileReader(traceFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                
                MemoryAccess memoryAccess = parseTraceFile(line);

                
                if (memoryAccess != null) {
                    simulateCache(memoryAccess, blockSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate derived metrics
        int totalCacheAccesses = cache.getTotalCacheAccesses();
        int cacheHits = 0;
        int compulsoryMisses = 0; 
        
        int cacheMisses = totalCacheAccesses - cacheHits;
        double hitRate = (double) cacheHits / totalCacheAccesses * 100;
        double missRate = 100 - hitRate;
        double baseCPI = 1.0; 
        double missPenalty = 20.0; 
        double cpi = calculateCPI(cacheHits, cache.getTotalCacheAccesses(), baseCPI, missPenalty);

        // Print simulation results
        System.out.println("***** CACHE SIMULATION RESULTS *****");
        System.out.println("***** CACHE SIMULATION RESULTS *****\n");
        System.out.println("Total Cache Accesses: " + totalCacheAccesses);
        System.out.println("Instruction Bytes: " + cache.instructionBytes);
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("--- Compulsory Misses: " + cache.compulsoryMisses);
        System.out.println("--- Conflict Misses: " + cache.conflictMisses);

        System.out.println("***** CACHE HIT & MISS RATE *****");
        System.out.println("Hit Rate: " + hitRate);
        System.out.println("Miss Rate: " + missRate);
        System.out.println("CPI: " + cpi);
        
        // Calculate unused cache space and waste
        double unusedKB = Math.max(((totalBlocks - compulsoryMisses) * (blockSize + overheadSize)) / 1024.0, 0);
        double waste = Math.max(cost / 1024.0 * unusedKB, 0);

        System.out.println("Unused Cache Space: " + String.format("%.2f", unusedKB) + " KB / " + (cacheSize * 1024) + " KB = " + 
        String.format("%.2f", unusedKB / (cacheSize * 1024) * 100) + "%" + "Waste: $" + String.format("%.2f", waste));
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
        private int size; 
        private int blockSize; 
        private int associativity; 
        private Map<Integer, CacheLine> cacheLines;
        private int totalCacheAccesses;
        private int totalCacheHits;
        private long instructionBytes;
        private int compulsoryMisses;
        private int conflictMisses;
        
        public Cache(int size, int blockSize, int associativity) {
            this.size = size;
            this.blockSize = blockSize;
            this.associativity = associativity;
            this.cacheLines = new HashMap<>();
            this.totalCacheAccesses = 0;

        }
        public void incrementHits() {
            totalCacheHits++;
        }
        public int getTotalCacheAccesses() {
            return totalCacheAccesses;
        }
        public int getTotalCacheHits() {
            return totalCacheHits;
        }
        public long getInstructionBytes() {
            return instructionBytes;
        }
        public int getConflictMisses() {
            return conflictMisses;
        }
        public void incrementConflictMisses() {
            conflictMisses++;
        }
    }

    // CacheLine class to represent a cache line and simulate cache line behavior
    static class CacheLine {
        private int associativity;
        private Map<Integer, Integer> tags;

        public CacheLine(int associativity) {
            this.associativity = associativity;
            this.tags = new HashMap<>();
        }

        public int simulateAccess(int tag, int blockNumber, int length) {

            if (tags.containsKey(tag)) {
                tags.put(tag, blockNumber);
            }
            // Increment totalCacheAccesses
            cache.totalCacheAccesses++;

            return length / 4; 
        }
        public boolean isFull() {
            return tags.size() >= associativity;
        }
    }
  
    private static double calculateCPI(int hits, int accesses, double baseCPI, double missPenalty) {
        if (accesses == 0) return baseCPI;
        double hitRate = (double) hits / accesses;
        return baseCPI + (1 - hitRate) * missPenalty;
    }
    
 
    private static MemoryAccess parseTraceFile(String line) {
       
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

        return null;
    }

    private static MemoryAccess parseInstructionLine(String line) {
        
        String[] parts = line.split(":");
        if (parts.length < 2) {
   
            return null;
        }

        String[] addressInfo = parts[1].trim().split("\\s+");
        if (addressInfo.length < 2) {
          
            return null;
        }

        String addressStr = addressInfo[1];

        try {
            
            return new MemoryAccess(Integer.parseInt(addressStr, 16), 0, true);
        } catch (NumberFormatException e) {
            
            System.out.println("Error parsing line: " + line);
            System.out.println("Exception message: " + e.getMessage());
            return null;
        }
    }


    private static MemoryAccess parseDstMLine(String line) {
        return null;
    }

    private static MemoryAccess parseSrcMLine(String line) {
        return null;
    }


    private static void simulateCache(MemoryAccess memoryAccess, int blockSize) {
        // Cache hit or miss logic
        int address = memoryAccess.getAddress();
        int length = memoryAccess.getLength();
        boolean isInstruction = memoryAccess.isInstruction();
   
        int bytesAccessedSimultaneously = 4;
        int blocksToRead = (int) Math.ceil((double) length / bytesAccessedSimultaneously);

        cache.totalCacheAccesses += blocksToRead;

        if (isInstruction) {
            cache.instructionBytes += length;
        }
       
        for (int i = 0; i < blocksToRead; i++) {
            
            int blockNumber = (address + i * bytesAccessedSimultaneously) / blockSize;
            int setIndex = blockNumber % (cache.size / (blockSize * cache.associativity));
            int tag = blockNumber / (cache.size / (blockSize * cache.associativity));
            
            CacheLine cacheLine = cache.cacheLines.computeIfAbsent(setIndex, k -> new CacheLine(cache.associativity));
            cacheLine.simulateAccess(tag, blockNumber, length);
            
            }

    }        
}

