package cz.gymjs.detekce;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.*;

public class GeneratePhotos {
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Zadat cestu k Mat Original [C:\\Users\\aaa\\Downloads\\xxx.jpg: ");
        String mriz = scanner.nextLine();
        if (mriz.isEmpty()) mriz = "C:\\Users\\mitka\\Downloads\\111.png";
        System.out.print("Zadat cestu k Mat plechovce [C:\\Users\\aaa\\Downloads\\xxz.jpg]: ");
        String plechovka = scanner.nextLine();
        if (plechovka.isEmpty()) plechovka = "C:\\Users\\mitka\\Downloads\\112.png";


        Mat image = Imgcodecs.imread(mriz);
        Mat lmaeft1 = Imgcodecs.imread(plechovka);


        System.out.println(lmaeft1.width() + "x" + lmaeft1.height());
        JFrame window = new JFrame();
        JFrame window1 = new JFrame();
        Mat vysledek = new Mat(image.size(), CV_8UC4);
        int l = 90;
        int r = 110;
        float a = 255.0f / (r - l);
        float b = l;
        Core.subtract(lmaeft1, new Scalar(b, b, b), vysledek);
        Core.multiply(vysledek, new Scalar(a, a, a), vysledek);
        Mat test = vysledek.clone();
        Mat kelner = getStructuringElement(MORPH_RECT, new Size(10, 10));
        Core.absdiff(image, vysledek, vysledek);
        Imgproc.morphologyEx(vysledek, vysledek, MORPH_OPEN, kelner);
        Imgproc.morphologyEx(vysledek, vysledek, MORPH_DILATE, kelner);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mat = new Mat();
        Imgproc.cvtColor(vysledek, vysledek, COLOR_RGB2GRAY);
        Imgproc.findContours(vysledek, contours, mat, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect bounds = Imgproc.boundingRect(points);
            int pointCount = points.toList().size();
            if (pointCount >= 4) {
                float ar = bounds.width / (float) bounds.height;
                System.out.println(bounds.width);
                System.out.println(bounds.height);
                System.out.println(ar);
                if (ar >= 0.50 && ar <= 0.8 && bounds.width <= 300) {
                    System.out.println("plechovka");
                    Imgproc.rectangle(test, bounds.tl(), bounds.br(), new Scalar( 255, 0, 255));
                } else {
                    System.out.println("neni plechovka");
                }
            } else {
                System.out.println("neni obdelnik");
            }
            //Imgproc.rectangle(image, bounds.tl(), bounds.br(), new Scalar(255, 0, 0), 1, 8, 0);

        }
        Util.imshow(vysledek, window);
        Util.imshow(test, window1);
    }
}

class Util {
    static void imshow(Mat img, JFrame frame) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
