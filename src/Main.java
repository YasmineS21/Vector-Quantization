import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.abs;

public class Main {

    public static void main(String[] args) throws IOException {
        File textFile = new File("sizes.txt");
        Scanner scanner = new Scanner(textFile);
        int blockHeight = 0;
        int blockWidth = 0;
        int codebookSize = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("Block Size:")) {
                String blockSize = line.split(":")[1].trim();
                String[] blockDimensions = blockSize.split("x");
                if (blockDimensions.length == 1) {
                    //single number -> square block (5 -> 5x5)
                    blockHeight = blockWidth = Integer.parseInt(blockDimensions[0]);
                } else if (blockDimensions.length == 2) {
                    // Two numbers,(5x5 -> 5x5)
                    blockHeight = Integer.parseInt(blockDimensions[0]);
                    blockWidth = Integer.parseInt(blockDimensions[1]);
                }
            } else if (line.startsWith("Codebook Size:")) {
                codebookSize = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        scanner.close();

//        System.out.println(blockHeight);
//        System.out.println(blockWidth);
//        System.out.println(codebookSize);


        VectorQuantization vq = new VectorQuantization();


        //compression output in text file
        vq.compress("images/woman.png", "compressed_data_woman.txt", blockHeight,blockWidth, codebookSize);
        vq.reconstruct("compressed_data_woman.txt", "reconstructed_woman.png");

        //compression output in binary file
        vq.compress("images/tree.png", "compressed_data_tree.bin", blockHeight,blockWidth, codebookSize);
        vq.reconstruct("compressed_data_tree.bin", "reconstructed_tree.png");

        //compression output in binary file
//        vq.compress("images/messi.png", "compressed_data_messi.bin", blockHeight,blockWidth, codebookSize);
//        vq.reconstruct("compressed_data_messi.bin", "reconstructed_messi.png");
//
//        //compression output in text file
//        vq.compress("images/lion.png", "compressed_data_lion.txt", blockHeight,blockWidth, codebookSize);
//        vq.reconstruct("compressed_data_lion.txt", "reconstructed_lion.png");

//        //compression output in binary file
//        vq.compress("images/butterfly.png", "compressed_data_butterfly.bin", blockHeight,blockWidth, codebookSize);
//        vq.reconstruct("compressed_data_butterfly.bin", "reconstructed_butterfly.png");

//        //compression output in text file
//        vq.compress("images/city.png", "compressed_data_city.txt", blockHeight,blockWidth, codebookSize);
//        vq.reconstruct("compressed_data_city.txt", "reconstructed_city.png");


    }
}