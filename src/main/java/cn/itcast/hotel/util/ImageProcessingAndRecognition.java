//package cn.itcast.hotel.util;
//
//import org.bytedeco.javacpp.Loader;
//import org.bytedeco.opencv.opencv_core.Mat;
//import org.bytedeco.opencv.opencv_core.Size;
//
//import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
//import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
//import static org.bytedeco.opencv.global.opencv_imgproc.*;
//
//public class ImageProcessingAndRecognition {
//
//    public static void main(String[] args) {
//        // Load OpenCV library
//        Loader.load(org.bytedeco.opencv.global.opencv_core.class);
//
//        // Load image
//        Mat image = imread("C:\\Users\\1\\Desktop\\phtone\\28\\1.jpg");
//
//        // Convert image to grayscale
//        Mat grayImage = new Mat();
//        cvtColor(image, grayImage, COLOR_BGR2GRAY);
//
//        // Apply binary thresholding
//        Mat binaryImage = new Mat();
//        threshold(grayImage, binaryImage, 0, 255, THRESH_BINARY_INV | THRESH_OTSU);
//
//        // Remove noise using morphology
//        Mat denoisedImage = new Mat();
//        Mat kernel = getStructuringElement(MORPH_RECT, new Size(3, 3));
//        morphologyEx(binaryImage, denoisedImage, MORPH_OPEN, kernel);
//
//        // Resize image to fit recognition model input size (e.g., 28x28)
//        Mat resizedImage = new Mat();
//        resize(denoisedImage, resizedImage, new Size(28, 28));
//
//        // Perform digit recognition
//        int recognizedDigit = recognizeDigit(resizedImage);
//
//        System.out.println("Recognized digit: " + recognizedDigit);
//    }
//
//    // Placeholder method for digit recognition
//    private static int recognizeDigit(Mat image) {
//        // Here you would implement your digit recognition algorithm
//        // This could involve using a pre-trained machine learning model or custom algorithm
//        // For simplicity, let's assume we return a random digit between 0 and 9
//        return (int) (Math.random() * 10);
//    }
//}