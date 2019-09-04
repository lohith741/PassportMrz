package PassportMrz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.abbyy.FREngine.CodePageEnum;
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IEngineLoader;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.ILicense;
import com.abbyy.FREngine.ILicenses;
import com.abbyy.FREngine.IPlainText;
import com.abbyy.FREngine.TextEncodingTypeEnum;

public class MrzExtraction {

	private static int minThreshValue = 60;
	private static int maxThreshValue = 180;
	private static int ThreshInterval = 10;
	private static final double MAX_HEIGHT_RATIO = 0.20;
	private static int MAX_NUMBER_IMAGE = 0;

	public static void main(String[] args) {
		IEngineLoader engineloader = Engine.CreateEngineOutprocLoader();
		IEngine pageEngine = engineloader.GetEngineObject("");
		ILicenses licenses = pageEngine.GetAvailableLicenses("SWTD11310005859608367284", "");
		ILicense pageLicense = licenses.Find("SWTD-1131-0006-3895-5638-2695");
		pageEngine.SetCurrentLicense(pageLicense, false);
		pageEngine.LoadPredefinedProfile("TextExtraction_Accuracy");

		System.load("C:\\dll\\opencv_java249.dll");
		String filePath1 = "D:/Development/temp/samples/New folder/test(14).jpg";
		
		//generating grayscale images
		List<String> imgePaths = getImageList1(filePath1);

		String line1 = "";
		
		// do ocr
		for (int i = 0; i < imgePaths.size(); i++) {
			doOcr(pageEngine, imgePaths.get(i));
		}

		// Extract char from images
		line1 = extractMrzCode(imgePaths);

		//generating images for second iteration 
		List<String> tempPaths1 = getImageList2(filePath1);
		
		//do ocr
		for (int i = 0; i < tempPaths1.size(); i++) {
			doOcr(pageEngine, tempPaths1.get(i));
		}
		
		//adding new generated images to image list
		imgePaths.addAll(tempPaths1);
		
		//extracting data from second iteration
		line1 = extractMrzCode(imgePaths);
		
	}
	
	//Generating Images from grayscale frequency "minThreshValue" to "maxThreshValue" with frequency "ThreshInterval"
	private static List<String> getImageList1(String filePath) {
		List<String> imgPaths = new ArrayList<String>();
		imgPaths.add(filePath);
		Mat img = null;
		Mat ref = null;
		Mat element = null;
		try {
			img = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_COLOR);
			for (int i = minThreshValue; i <= maxThreshValue; i = i + ThreshInterval) {
				ref = new Mat();
				img.copyTo(ref);
				String dstImgPath = FilenameUtils.removeExtension(filePath) + "_" + i + ".png";
				Imgproc.cvtColor(ref, ref, Imgproc.COLOR_BGR2GRAY);
				Imgproc.threshold(ref, ref, i, 255, Imgproc.THRESH_BINARY_INV);

				int size = 1;
				element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * size + 1, 2 * size + 1));
				Imgproc.dilate(ref, ref, element);
				Imgproc.erode(ref, ref, element);

				Highgui.imwrite(dstImgPath, ref);
				imgPaths.add(dstImgPath);
			}
		} catch (Exception e) {
		} finally {
			img = null;
			ref = null;
			element = null;
			System.gc();
		}
		return imgPaths;
	}

	//Generating images for second iteration
	private static List<String> getImageList2(String filePath) {
		List<String> imgPaths = new ArrayList<String>();
		Mat img = null;
		Mat ref = null;
		Mat element = null;
		try {
			int mid = (int) (minThreshValue + MAX_NUMBER_IMAGE * ThreshInterval);
			int min = mid - 15;
			if (min < 0) {
				min = 0;
			}
			int max = mid + 15;
			if (max > 255) {
				max = 255;
			}
			img = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_COLOR);
			for (int i = min; i <= max; i = i + 3) {
				int temp = i - minThreshValue;
				if (temp >= 0 && temp <= maxThreshValue - minThreshValue) {
					if (temp % ThreshInterval == 0) {
						continue;
					}
				}
				ref = new Mat();
				img.copyTo(ref);
				String dstImgPath = FilenameUtils.removeExtension(filePath) + "_" + i + ".png";
				Imgproc.cvtColor(ref, ref, Imgproc.COLOR_BGR2GRAY);
				Imgproc.threshold(ref, ref, i, 255, Imgproc.THRESH_BINARY_INV);

				int size = 1;
				element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * size + 1, 2 * size + 1));
				Imgproc.dilate(ref, ref, element);
				Imgproc.erode(ref, ref, element);

				Highgui.imwrite(dstImgPath, ref);
				imgPaths.add(dstImgPath);
			}
		} catch (Exception e) {
		} finally {
			img = null;
			ref = null;
			element = null;
			System.gc();
		}
		return imgPaths;
	}

	//Extracting data
	private static String extractMrzCode(List<String> imgePaths) {
		TreeMap<Double, HashMap<String, Integer>> charMap = new TreeMap<Double, HashMap<String, Integer>>();
		TreeMap<Double, HashMap<String, Integer>> charMap2 = new TreeMap<Double, HashMap<String, Integer>>();
		TreeMap<Double, HashMap<String, Integer>> filterCharMap = new TreeMap<Double, HashMap<String, Integer>>();
		TreeMap<Double, HashMap<String, Integer>> filterCharMap2 = new TreeMap<Double, HashMap<String, Integer>>();
		TreeMap<Double, Integer> charWidth = new TreeMap<Double, Integer>();
		TreeMap<Double, Integer> charHeight = new TreeMap<Double, Integer>();
		PriorityQueue<Double> indexQueue = new PriorityQueue<>();
		PriorityQueue<Double> indexQueue2 = new PriorityQueue<>();

		List<List<CharCoordinate>> charsList = new ArrayList<>();
		int maxNumberDigit = Integer.MIN_VALUE;
		for (int i = 0; i < imgePaths.size(); i++) {
			List<CharCoordinate> chars = getSortDigit(imgePaths.get(i));
			if (chars.size() > 4) {
				for (int j = 0; j < chars.size(); j++) {
					double width = Math.abs(chars.get(j).getRight() - chars.get(j).getLeft());
					if (!charWidth.containsKey(width)) {
						charWidth.put(width, 0);
					}
					charWidth.put(width, charWidth.get(width) + 1);

					double height = Math.abs(chars.get(j).getTop() - chars.get(j).getBottom());
					if (!charHeight.containsKey(height)) {
						charHeight.put(height, 0);
					}
					charHeight.put(height, charHeight.get(height) + 1);
				}
				if (i != 0 && maxNumberDigit < chars.size()) {
					MAX_NUMBER_IMAGE = i;
					maxNumberDigit = chars.size();
				}
				charsList.add(chars);
			}
		}

		double maxWidth = 0;
		int maxWidthFreq = Integer.MIN_VALUE;
		for (Entry<Double, Integer> e : charWidth.entrySet()) {
			if (maxWidthFreq < e.getValue()) {
				maxWidthFreq = e.getValue();
				maxWidth = e.getKey();
			}
		}
		maxWidth = maxWidth * 0.75;

		double maxHeight = 0;
		int maxHeightFreq = Integer.MIN_VALUE;
		for (Entry<Double, Integer> e : charHeight.entrySet()) {
			if (maxHeightFreq < e.getValue()) {
				maxHeightFreq = e.getValue();
				maxHeight = e.getKey();
			}
		}

		for (int i = 0; i < charsList.size(); i++) {
			List<CharCoordinate> chars = charsList.get(i);
			double left = Integer.MIN_VALUE;
			boolean firstLine = true;
			for (int j = 0; j < chars.size(); j++) {
				double height = Math.abs(chars.get(j).getTop() - chars.get(j).getBottom());
				if (Math.abs(height - maxHeight) > maxHeight * MAX_HEIGHT_RATIO) {
					continue;
				}
				if(Double.compare(left, chars.get(j).getLeft()) < 0 && firstLine) {
				if (!charMap.containsKey(chars.get(j).getLeft())) {
					charMap.put(chars.get(j).getLeft(), new HashMap<String, Integer>());
					indexQueue.add(chars.get(j).getLeft());
				}
				if (!charMap.get(chars.get(j).getLeft()).containsKey(chars.get(j).getString())) {
					charMap.get(chars.get(j).getLeft()).put(chars.get(j).getString(), 0);
				}
				int prevVal = charMap.get(chars.get(j).getLeft()).get(chars.get(j).getString());
				charMap.get(chars.get(j).getLeft()).put(chars.get(j).getString(), prevVal + 1);
				left = chars.get(j).getLeft();
				}
				else {
					if (!charMap2.containsKey(chars.get(j).getLeft())) {
						charMap2.put(chars.get(j).getLeft(), new HashMap<String, Integer>());
						indexQueue2.add(chars.get(j).getLeft());
					}
					if (!charMap2.get(chars.get(j).getLeft()).containsKey(chars.get(j).getString())) {
						charMap2.get(chars.get(j).getLeft()).put(chars.get(j).getString(), 0);
					}
					int prevVal = charMap2.get(chars.get(j).getLeft()).get(chars.get(j).getString());
					charMap2.get(chars.get(j).getLeft()).put(chars.get(j).getString(), prevVal + 1);
					firstLine = false;
					}
			}
		}

		//extracting first line
		while (!indexQueue.isEmpty()) {
			Double min1 = indexQueue.poll();
			Double min2 = indexQueue.poll();
			if (min1 == null) {
				break;
			}
			if (min2 == null) {
				break;
			}
			if (Math.abs(min1 - min2) < maxWidth) {
				double mid = (min1 + min2) / 2;
				HashMap<String, Integer> char1 = charMap.get(min1);
				HashMap<String, Integer> char2 = charMap.get(min2);
				charMap.remove(min1);
				charMap.remove(min2);

				HashMap<String, Integer> charRes = new HashMap<>();
				for (Entry<String, Integer> i : char1.entrySet()) {
					if (!charRes.containsKey(i.getKey())) {
						charRes.put(i.getKey(), i.getValue());
					} else {
						charRes.put(i.getKey(), charRes.get(i.getKey()) + i.getValue());
					}
				}

				for (Entry<String, Integer> i : char2.entrySet()) {
					if (!charRes.containsKey(i.getKey())) {
						charRes.put(i.getKey(), i.getValue());
					} else {
						charRes.put(i.getKey(), charRes.get(i.getKey()) + i.getValue());
					}
				}

				charMap.put(mid, charRes);
				indexQueue.add(mid);
			} else {
				indexQueue.add(min2);
			}
		}
		filterCharMap = charMap;

		TreeMap<Integer, TreeMap<Double, String>> maxChar = new TreeMap<Integer, TreeMap<Double, String>>(
				Collections.reverseOrder());
		for (Entry<Double, HashMap<String, Integer>> e : filterCharMap.entrySet()) {
			int maxFr = Integer.MIN_VALUE;
			String charStr = "";
			for (Entry<String, Integer> m : e.getValue().entrySet()) {
				if (maxFr < m.getValue()) {
					maxFr = m.getValue();
					charStr = m.getKey();
				}
			}
			if (!maxChar.containsKey(maxFr)) {
				maxChar.put(maxFr, new TreeMap<Double, String>());
			}
			if (!maxChar.get(maxFr).containsKey(e.getKey())) {
				maxChar.get(maxFr).put(e.getKey(), charStr);
			}
		}
		
		TreeMap<Double, String> result = new TreeMap<>();
		for (Entry<Integer, TreeMap<Double, String>> i : maxChar.entrySet()) {
			for (Entry<Double, String> j : i.getValue().entrySet()) {
				if (result.size() < 44) {
					if (!result.containsKey(j.getKey())) {
						result.put(j.getKey(), j.getValue());
					}
				} else {
					break;
				}
			}
		}
		String mrz = "";
		for (Entry<Double, String> e : result.entrySet()) {
			mrz = mrz + e.getValue();
		}
		System.out.print("line1- ");
		System.out.println(mrz);
		System.out.println(mrz.length());
		
		//extracting second line
		while (!indexQueue2.isEmpty()) {
			Double min1 = indexQueue2.poll();
			Double min2 = indexQueue2.poll();
			if (min1 == null) {
				break;
			}
			if (min2 == null) {
				break;
			}
			if (Math.abs(min1 - min2) < maxWidth) {
				double mid = (min1 + min2) / 2;
				HashMap<String, Integer> char1 = charMap2.get(min1);
				HashMap<String, Integer> char2 = charMap2.get(min2);
				charMap2.remove(min1);
				charMap2.remove(min2);

				HashMap<String, Integer> charRes2 = new HashMap<>();
				for (Entry<String, Integer> i : char1.entrySet()) {
					if (!charRes2.containsKey(i.getKey())) {
						charRes2.put(i.getKey(), i.getValue());
					} else {
						charRes2.put(i.getKey(), charRes2.get(i.getKey()) + i.getValue());
					}
				}

				for (Entry<String, Integer> i : char2.entrySet()) {
					if (!charRes2.containsKey(i.getKey())) {
						charRes2.put(i.getKey(), i.getValue());
					} else {
						charRes2.put(i.getKey(), charRes2.get(i.getKey()) + i.getValue());
					}
				}

				charMap2.put(mid, charRes2);
				indexQueue2.add(mid);
			} else {
				indexQueue2.add(min2);
			}
		}
		filterCharMap2 = charMap2;
		
		maxChar = new TreeMap<Integer, TreeMap<Double, String>>(
				Collections.reverseOrder());
		for (Entry<Double, HashMap<String, Integer>> e : filterCharMap2.entrySet()) {
			int maxFr = Integer.MIN_VALUE;
			String charStr = "";
			for (Entry<String, Integer> m : e.getValue().entrySet()) {
				if (maxFr < m.getValue()) {
					maxFr = m.getValue();
					charStr = m.getKey();
				}
			}
			if (!maxChar.containsKey(maxFr)) {
				maxChar.put(maxFr, new TreeMap<Double, String>());
			}
			if (!maxChar.get(maxFr).containsKey(e.getKey())) {
				maxChar.get(maxFr).put(e.getKey(), charStr);
			}
		}
		
		result = new TreeMap<>();
		for (Entry<Integer, TreeMap<Double, String>> i : maxChar.entrySet()) {
			for (Entry<Double, String> j : i.getValue().entrySet()) {
				if (result.size() < 44) {
					if (!result.containsKey(j.getKey())) {
						result.put(j.getKey(), j.getValue());
					}
				} else {
					break;
				}
			}
		}

		String mrz2 = "";
		for (Entry<Double, String> e : result.entrySet()) {
			mrz2 = mrz2 + e.getValue();
		}
		System.out.print("line2- ");
		System.out.println(mrz2);
		System.out.println(mrz2.length());

		String finalMrz = mrz.concat(mrz2);
		return finalMrz;
	}

	//get character coordinates and generate charlist
	private static List<CharCoordinate> getSortDigit(String filepath) {
		List<CharCoordinate> chars = new XmlUtility(filepath).getCharCoordinateList();
		List<CharCoordinate> chars1 = new ArrayList<CharCoordinate>();
		for (int i = 0; i < chars.size(); i++) {
			if (chars.get(i).getString() == null || chars.get(i).getString().trim().isEmpty()) {
				continue;
			}

			if (Character.isDigit(chars.get(i).getString().trim().charAt(0))
					|| Character.isUpperCase(chars.get(i).getString().trim().charAt(0))
					|| chars.get(i).getString().trim().charAt(0) == '<') {

				chars1.add(chars.get(i));
			}
		}

		return chars1;
	}

	public static void doOcr(IEngine pageEngine, String imgePath) {
		try {

			String txtFilePath = FilenameUtils.removeExtension(imgePath) + ".txt";
			String xmlFilePath = FilenameUtils.removeExtension(imgePath) + ".xml";
			IFRDocument document = pageEngine.CreateFRDocumentFromImage(imgePath, null);
			document.Process(null);
			IPlainText ip = document.getPlainText();
			ip.SaveToAsciiXMLFile(xmlFilePath);
			ip.SaveToTextFile(txtFilePath, TextEncodingTypeEnum.TET_Simple, CodePageEnum.CP_Latin);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
