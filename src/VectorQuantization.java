import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class VectorQuantization {

//=========>COMPRESSION // CONSTRUCTION <============\\

    public void compress(String inputImg, String outputFile, int blockHeight, int blockWidth, int codeBookSize) throws IOException {
        BufferedImage img = ImageIO.read(new File(inputImg));
        int imgHeight = img.getHeight();
        int imgWidth = img.getWidth();

        // 1-divide image into blocks
        List<int[][]> Blocks = divideImgIntoBlocks(img, blockHeight,blockWidth);

        // 2-get the average block
        // 3-LBG splitting
        // 4-get codebook (assign each block to closest block, get avg, split, assign, etc)
        List<int[][]> CodeBook = generateCodeBook(getAvgBlock(Blocks), codeBookSize, Blocks);

        // 5-replace each block with the index of
        // closest codebook block
        int[][] compressedMatrix = generateCompressedMatrix(Blocks, CodeBook, imgHeight, imgWidth, blockHeight,blockWidth);

        // 6-save the compressed data to a file
        saveCompressedData(outputFile, compressedMatrix, CodeBook, imgHeight, imgWidth, blockHeight,blockHeight);
    }

    private List<int[][]> divideImgIntoBlocks(BufferedImage img, int blockHeight,int blockWidth) {
        List<int[][]> blocks = new ArrayList<>();
        int imgHeight = img.getHeight();
        int imgWidth = img.getWidth();

        int fullHeight = (imgHeight / blockHeight) * blockHeight;
        int fullWidth = (imgWidth / blockWidth) * blockWidth;
        //ba3mel skip lel pixels el fel sora el mesh fit guwa el blocks 3ashan kan beygeeb errors.

        for (int i = 0; i < fullHeight; i += blockHeight) {
            for (int j = 0; j < fullWidth; j += blockWidth) {
                int[][] block = new int[blockHeight][blockWidth];
                for (int y = 0; y < blockHeight; y++) {
                    for (int x = 0; x < blockWidth; x++) {
                        int rgb = img.getRGB(j + x, i + y);
                        int grayscale = rgb & 0xFF; //bakhod haga wahda mel rgb 3ashan 3ayza grayscale
                        block[y][x] = grayscale;
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    private int[][] getAvgBlock(List<int[][]> Blocks) {
        int rows = Blocks.get(0).length;
        int cols = Blocks.get(0)[0].length;

        int[][] avgBlock = new int[rows][cols];

        for (int i = 0; i < rows; i++) {//loop 3alla kol indices el f block wahda
            for (int j = 0; j < cols; j++) {
                int sum = 0;
                for (int[][] block : Blocks) {
                    sum += block[i][j];
                }
                avgBlock[i][j] = sum / Blocks.size();
            }
        }
        return avgBlock;
    }

    private List<int[][]> generateCodeBook(int[][] avgBlock, int codebookSize, List<int[][]> Blocks) {
        List<int[][]> codebook = new ArrayList<>();
        codebook.add(avgBlock);
        while (codebook.size() < codebookSize) {
            List<int[][]> updatedCodebook = new ArrayList<>();
            for (int[][] block : codebook) {
                int[][] splitBlock1 = new int[block.length][block[0].length];
                int[][] splitBlock2 = new int[block.length][block[0].length];

                for (int i = 0; i < block.length; i++) {
                    for (int j = 0; j < block[0].length; j++) {
                        splitBlock1[i][j] = block[i][j] - 1;
                        splitBlock2[i][j] = block[i][j] + 1;
                    }
                }
                updatedCodebook.add(splitBlock1);
                updatedCodebook.add(splitBlock2);
            }

            List<List<int[][]>> assignedBlocks = assignBlocksToAvgBlock(Blocks, updatedCodebook);
            codebook.clear();
            for (List<int[][]> blocks : assignedBlocks) {
                if (!blocks.isEmpty()) {
                    codebook.add(getAvgBlock(blocks));
                }
            }
        }
        return codebook;
    }

    private List<List<int[][]>> assignBlocksToAvgBlock(List<int[][]> Blocks, List<int[][]> currentCodebook) {
        List<List<int[][]>> collectionOfBlocks = new ArrayList<>(); //idx = num of codebook,has list of blocks
        for (int i = 0; i < currentCodebook.size(); i++) {
            collectionOfBlocks.add(new ArrayList<>());
            //initialize
        }

        for (int[][] block : Blocks) {
            int idxOfMinDistance = findClosestCodebookBlock(block, currentCodebook);
            collectionOfBlocks.get(idxOfMinDistance).add(block); //idxOfCodeblockOfMinDistanceFromBlock
        }
        return collectionOfBlocks;
    }

    private int findClosestCodebookBlock(int[][] block, List<int[][]> currentCodebook) {
        int idxOfMinDistance = 0;
        double minDistance = calculateDistance(block, currentCodebook.get(0)); //with cb0

        for (int i = 1; i < currentCodebook.size(); i++) {
            double distance = calculateDistance(block, currentCodebook.get(i)); //rest of cbs
            if (distance < minDistance) {
                minDistance = distance;
                idxOfMinDistance = i;
            }
        }
        return idxOfMinDistance; //idx of min codeblock to that block
    }

    private double calculateDistance(int[][] block, int[][] codebookBlock) {
        double totalDistance = 0;
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                double difference = abs(block[i][j] - codebookBlock[i][j]);
                totalDistance += difference;
            }
        }
        return totalDistance;
    }

    private int[][] generateCompressedMatrix(List<int[][]> blocks, List<int[][]> codebook, int imgHeight, int imgWidth, int blockHeight , int blockWidth) {
        int numRows = imgHeight / blockHeight;
        int numCols = imgWidth / blockWidth;
        int[][] compressedMatrix = new int[numRows][numCols];

        //numRowsxnumCols = block size
        for (int idx = 0; idx < numRows * numCols; idx++) {
            int row = idx / numCols; //2d without double forloop ba3d ma bawazt el heap
            int col = idx % numCols;
            int closestIndex = findClosestCodebookBlock(blocks.get(idx), codebook);
            compressedMatrix[row][col] = closestIndex;
        }
        return compressedMatrix;
    }

    private void saveCompressedData(String outputFile, int[][] compressedMatrix, List<int[][]> codebook, int imgHeight, int imgWidth, int blockHeight , int blockWidth) throws IOException {

        if (outputFile.endsWith(".txt")) {
            saveCompressedDataTxt(outputFile, compressedMatrix, codebook, imgHeight, imgWidth, blockHeight , blockWidth);
        } else if (outputFile.endsWith(".bin")) {
            saveCompressedDataBinary(outputFile, compressedMatrix, codebook, imgHeight, imgWidth, blockHeight,blockWidth);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please use .txt or .bin.");
        }
    }

    private void saveCompressedDataTxt(String outputFile, int[][] compressedMatrix, List<int[][]> codebook, int imgHeight, int imgWidth, int blockHeight , int blockWidth) throws IOException {
        int bitsRequired = (int) Math.ceil(Math.log(codebook.size()) / Math.log(2));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("Image Height: " + imgHeight);
            writer.newLine();
            writer.write("Image Width: " + imgWidth);
            writer.newLine();
            writer.write("Block Height: " + blockHeight);
            writer.newLine();
            writer.write("Block Width: " + blockWidth);
            writer.newLine();
            writer.write("Codebook Size: " + codebook.size());
            writer.newLine();
            writer.write("Bits Required: " + bitsRequired);
            writer.newLine();
            writer.newLine();

            writer.write("Compressed Matrix (Binary):");
            writer.newLine();
            for (int[] row : compressedMatrix) {
                for (int index : row) {
                    String binaryString = Integer.toBinaryString(index);
                    while (binaryString.length() < bitsRequired) {
                        binaryString = "0" + binaryString;
                    }
                    writer.write(binaryString + " ");
                }
                writer.newLine();
            }
            writer.newLine();

            writer.write("Codebook:");
            writer.newLine();
            for (int i = 0; i < codebook.size(); i++) {
                writer.write("Codebook Block " + i + ":");
                writer.newLine();
                int[][] block = codebook.get(i);
                for (int[] row : block) {
                    for (int value : row) {
                        writer.write(value + " ");
                    }
                    writer.newLine();
                }
                writer.newLine();
            }
        }
        System.out.println("Compression completed successfully!");
    }

    private void saveCompressedDataBinary(String outputFile, int[][] compressedMatrix, List<int[][]> codebook, int imgHeight, int imgWidth, int blockHeight , int blockWidth) throws IOException {
        //int bitsRequired = (int) Math.ceil(Math.log(codebook.size()) / Math.log(2));

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
            dos.writeInt(imgHeight);
            dos.writeInt(imgWidth);
            dos.writeInt(blockHeight);
            dos.writeInt(blockWidth);
            dos.writeInt(codebook.size());

            //compressed matrix
            for (int[] row : compressedMatrix) {
                for (int index : row) {
                    dos.writeInt(index);
                }
            }

            //codebook
            for (int[][] block : codebook) {
                for (int[] row : block) {
                    for (int value : row) {
                        dos.writeInt(value);
                    }
                }
            }

        }
        System.out.println("Compression (Binary) completed successfully!");
    }

    //=========>DECOMPRESSION // RECONSTRUCTION <============\\
    private ReconstructionData readFromTextFile(String inputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            int imgHeight = Integer.parseInt(reader.readLine().split(": ")[1]);
            int imgWidth = Integer.parseInt(reader.readLine().split(": ")[1]);
            int blockHeight = Integer.parseInt(reader.readLine().split(": ")[1]);
            int blockWidth = Integer.parseInt(reader.readLine().split(": ")[1]);
            int codebookSize = Integer.parseInt(reader.readLine().split(": ")[1]);
            int bitsRequired = Integer.parseInt(reader.readLine().split(": ")[1]);
            reader.readLine(); //empty line


            reader.readLine(); //skip "Compressed Matrix (Binary):"
            List<int[]> compressedMatrix = new ArrayList<>();
            String line;
            while (!(line = reader.readLine()).equals("")) {
                String[] binaryStrings = line.split(" ");
                int[] row = new int[binaryStrings.length];
                for (int i = 0; i < binaryStrings.length; i++) {
                    row[i] = Integer.parseInt(binaryStrings[i], 2); //convert binary to int
                }
                compressedMatrix.add(row);
            }

            reader.readLine(); //skip "Codebook:"
            List<int[][]> codebook = new ArrayList<>();
            int blocksRead = 0;
            while (blocksRead < codebookSize && (line = reader.readLine()) != null) {
                if (line.contains("Codebook Block")) {
                    int[][] block = new int[blockHeight][blockWidth];
                    for (int j = 0; j < blockHeight; j++) {
                        String[] values = reader.readLine().split(" ");
                        for (int k = 0; k < blockWidth; k++) {
                            block[j][k] = Integer.parseInt(values[k]);
                        }
                    }
                    codebook.add(block);
                    blocksRead++;
                }
            }

            int[][] compressedMatrixArray = compressedMatrix.toArray(new int[0][0]);
            return new ReconstructionData(imgHeight, imgWidth, blockHeight, blockWidth, codebook, compressedMatrixArray);
        }
    }

    private ReconstructionData readFromBinaryFile(String binaryFilePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(binaryFilePath))) {
            int imgHeight = dis.readInt();
            int imgWidth = dis.readInt();
            int blockHeight = dis.readInt();
            int blockWidth = dis.readInt();
            int codebookSize = dis.readInt();

            int[][] compressedMatrix = new int[imgHeight / blockHeight][imgWidth / blockWidth];
            for (int i = 0; i < compressedMatrix.length; i++) {
                for (int j = 0; j < compressedMatrix[0].length; j++) {
                    compressedMatrix[i][j] = dis.readInt();
                }
            }

            List<int[][]> codebook = new ArrayList<>();
            for (int i = 0; i < codebookSize; i++) {
                int[][] block = new int[blockHeight][blockWidth];
                for (int j = 0; j < blockHeight; j++) {
                    for (int k = 0; k < blockWidth; k++) {
                        block[j][k] = dis.readInt();
                    }
                }
                codebook.add(block);
            }


            return new ReconstructionData(imgHeight, imgWidth, blockHeight, blockWidth, codebook, compressedMatrix);
        }
    }

    private BufferedImage reconstruction(ReconstructionData data) {
        int imgHeight = data.imgHeight;
        int imgWidth = data.imgWidth;
        int blockHeight = data.blockHeight;
        int blockWidth = data.blockWidth;
        List<int[][]> codebook = data.codebook;
        int[][] compressedMatrix = data.compressedMatrix;

        BufferedImage reconstructedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < compressedMatrix.length; i++) {//blocks in the compressed matrix
            for (int j = 0; j < compressedMatrix[0].length; j++) {
                int blockIndex = compressedMatrix[i][j];
                int[][] codeBlock = codebook.get(blockIndex);

                //put the codebook block back into the image
                for (int y = 0; y < blockHeight; y++) {
                    for (int x = 0; x < blockWidth; x++) {
                        //image bounds
                        if (i * blockHeight + y < imgHeight && j * blockWidth + x < imgWidth) {
                            int grayscale = codeBlock[y][x];
                            int rgb = (grayscale << 16) | (grayscale << 8) | grayscale;
                            reconstructedImage.setRGB(j * blockWidth + x, i * blockHeight + y, rgb);
                        }
                    }
                }
            }
        }

        return reconstructedImage;
    }

    public void reconstruct(String inputFile, String outputImage) throws IOException {
        ReconstructionData data;
        if (inputFile.endsWith(".txt")) {
            data = readFromTextFile(inputFile);
        } else if (inputFile.endsWith(".bin")) {
            data = readFromBinaryFile(inputFile);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please use .txt or .bin.");
        }

        BufferedImage reconstructedImage = reconstruction(data);

        File outputFile = new File(outputImage);
        ImageIO.write(reconstructedImage, "png", outputFile);
        System.out.println("Reconstructed image saved to: " + outputImage);
    }
}