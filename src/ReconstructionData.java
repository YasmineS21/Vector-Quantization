import java.util.List;

public class ReconstructionData {
    int imgHeight;
    int imgWidth;
    int blockHeight;
    int blockWidth;
    List<int[][]> codebook;
    int[][] compressedMatrix;

    public ReconstructionData(int imgHeight, int imgWidth, int blockHeight, int blockWidth, List<int[][]> codebook, int[][] compressedMatrix) {
        this.imgHeight = imgHeight;
        this.imgWidth = imgWidth;
        this.blockHeight = blockHeight;
        this.blockWidth = blockWidth;
        this.codebook = codebook;
        this.compressedMatrix = compressedMatrix;
    }
}
