package opencv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class utils {
	public static Mat LectureImage(String fichier) {
		File f = new File(fichier);
		Mat m = Highgui.imread(f.getAbsolutePath());
		return m;
	}
	public static void Imshow(String title, Mat img) {
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".png", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try { 
			InputStream in = new ByteArrayInputStream(byteArray); 
			bufImage = ImageIO.read(in); 
			JFrame frame = new JFrame(); 
			frame.setTitle(title); 
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage))); 
			frame.pack(); 
			frame.setVisible(true); 
		} 
		catch (Exception e) { 
			e.printStackTrace(); 
		} 
	}
	public static Mat DetecterObjetRouge(Mat hsv_image) {
		Mat threshold_img1 = new Mat(); 
		Mat threshold_img2 = new Mat(); 
		Mat threshold_img = new Mat(); 
		Core. inRange(hsv_image, new Scalar(0,100,100), new Scalar(10,255,255), threshold_img1); 
		Core. inRange(hsv_image, new Scalar(160,100,100), new Scalar(179,255,255), threshold_img2); 
		Core. bitwise_or(threshold_img1, threshold_img2, threshold_img); 
		Imgproc.GaussianBlur(threshold_img, threshold_img, new Size(9, 9), 2.0, 2.0); 
		return threshold_img; 
	}
	public static List<MatOfPoint> DetecterContours(Mat threshold_img) {

		int thresh = 100; 
		Mat canny_output = new Mat(); 
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); 
		MatOfInt4 hierarchy = new MatOfInt4(); 
		Imgproc.Canny( threshold_img, canny_output, thresh, thresh*2); 
		Imgproc.findContours( canny_output, contours, hierarchy,Imgproc.RETR_EXTERNAL, 
				Imgproc.CHAIN_APPROX_SIMPLE); 		
		return contours; 
	}

	public static Mat Scaling(Mat Object, Mat sroadSign) {

		Mat sObject = new Mat(); 
		Imgproc.resize(Object, sObject, sroadSign.size()); 
		Mat grayObject = new Mat(sObject.rows(), sObject.cols(), sObject.type()); 
		Imgproc.cvtColor(sObject, grayObject, Imgproc.COLOR_BGRA2GRAY); 
		Core.normalize(grayObject, grayObject, 0, 255, Core.NORM_MINMAX); 	
		return grayObject;
	}

	public static Mat SigntoGray(Mat sroadSign) {
		Mat graySign = new Mat(sroadSign.rows(), sroadSign.cols(), sroadSign.type()); 
		Imgproc.cvtColor(sroadSign, graySign, Imgproc.COLOR_BGRA2GRAY); 
		Core.normalize(graySign, graySign, 0, 255, Core.NORM_MINMAX); 
		return graySign;
	}

	public static Mat extractRoadSign(Mat img) {
		
		Mat m = img;
		Mat cuttedImg = new Mat();
		Mat hsv_image = Mat.zeros(m.size(),m.type()); 
		Imgproc.cvtColor(m, hsv_image, Imgproc.COLOR_BGR2HSV); 

		Mat threshold_img = utils.DetecterObjetRouge(hsv_image); 
		List<MatOfPoint> contours = utils.DetecterContours(threshold_img); 
		MatOfPoint2f matOfPoint2f = new MatOfPoint2f(); 
		float[] radius = new float[1]; 
		Point center = new Point(); 
		for (int c=0; c < contours.size();c++) {
			MatOfPoint contour = contours.get(c); 
			double contourArea = Imgproc.contourArea(contour); 
			matOfPoint2f.fromList(contour.toList()); 
			Imgproc.minEnclosingCircle(matOfPoint2f, center, radius); 
			if ((contourArea/(Math.PI*radius[0]*radius[0])) >=0.7) { 
				Core.circle(m, center, (int)radius[0], new Scalar(0, 255, 0), 2); 
				Rect rect = Imgproc.boundingRect(contour); 
				Core.rectangle(m, new Point(rect.x,rect.y), 
						new Point(rect.x+rect.width,rect.y+rect.height), 
						new Scalar (0, 255, 0), 2); 
				Mat tmp = m.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width); 
				Mat ball = Mat.zeros(tmp.size(),tmp.type()); 
				tmp.copyTo(ball); 
				//utils.Imshow("Ball",ball); 
				cuttedImg = ball;
			}
		}	
		return cuttedImg;
	}

}