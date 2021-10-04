
/* Class to handle matrix operations done for the project 
 * and to separate code
 * */
public class MatrixOp {
	private final int SIZE = 8;
	private double[][] qTable = { {1,1,2,4,8,16,32,64},
								  {1,1,2,4,8,16,32,64},
								  {2,2,2,4,8,16,32,64},
								  {4,4,4,4,8,16,32,64},
								  {8,8,8,8,8,16,32,64},
								  {16,16,16,16,16,16,32,64},
								  {32,32,32,32,32,32,32,64},
								  {64,64,64,64,64,64,64,64}};
	
	public MatrixOp() {
	
		
	}
	
	public double[][] inverseTransform(double[][] input){
		double[][] tMatrix = getTransformMatrix();
		double[][] transpose = tranposeMatrix(tMatrix);
		double[][] y = new double[SIZE][SIZE];
		double[][] output = new double[SIZE][SIZE];
		
		// Y * T
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input[i].length; j++) {
				for(int k = 0; k < y.length; k++) {
					y[i][j] += (input[i][k] * tMatrix[k][j]);
				}
			}
		}
		
		// T^T * (YT)
		for(int i = 0; i < y.length; i++) {
			for(int j = 0; j < y[i].length; j++) {
				for(int k = 0; k < output.length; k++) {
					output[i][j] += (transpose[i][k] * y[k][j]);
				}
			}
		}
		roundMatrix(output);
		
		return output;
	}
	
	public void dequantize(double[][] input) {
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input.length; j++) {
				input[i][j] = Math.round(input[i][j] * qTable[i][j]);
			}
		}
	}
	
	public void quantize(double[][] input) {
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input[i].length; j++) {
				input[i][j] = Math.round(input[i][j] / qTable[i][j]);
			}
		}
	}
	
	public double[][] tranposeMatrix(double[][] input){
		double[][] transpose = new double[SIZE][SIZE];
		
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input.length; j++) {
				transpose[i][j] = input[j][i];
			}
		}
		
		return transpose;
	}
	
	public double[][] getTransformMatrix(){
		double[][] out = new double[SIZE][SIZE];
		double a;
		double c;
		
		for(int i = 0; i < out.length; i++) {
			for(int j = 0; j < out[i].length; j++) {
				if(i == 0)
					a = Math.sqrt(1.0 / out.length);
				else
					a = Math.sqrt(2.0 / out.length);
				
				c = a * Math.cos(((2 * j + 1) * i * Math.PI) / (2 * out.length));
				
				out[i][j] = c;
			}
		}
		
		return out;
	}
	
	public double[][] DCT(double[][] input){
		double[][] tMatrix = getTransformMatrix();
		double[][] transpose = tranposeMatrix(tMatrix);
		double[][] a = new double[SIZE][SIZE];
		double[][] output = new double[SIZE][SIZE];
		
		// T * X
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input[i].length; j++) {
				for(int k = 0; k < a.length; k++) {
					a[i][j] += (tMatrix[i][k] * input[k][j]);
				}
			}
		}
		
		// (TX) * T^T
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[i].length; j++) {
				for(int k = 0; k < output.length; k++) {
					output[i][j] += (a[i][k] * transpose[k][j]);
				}
			}
		}
		roundMatrix(output);
		
		return output;
	}
	
	public void printMatrix(double[][] matrix) {
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public void roundMatrix(double[][] input) {
		for(int i = 0; i < input.length; i++) {
			for(int j = 0; j < input[i].length; j++) {
				input[i][j] = Math.round(input[i][j]);
			}
		}
	}
	
}
