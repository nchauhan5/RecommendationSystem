package com.recomm;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Recomm {

	private static String JSON_FILE = "<Full path to json file>\\recomm_json.json";
	static HashMap<String, HashMap<String, Double>> movieDataMap = new HashMap<String, HashMap<String,Double>>();
	static HashMap<String, Double> similarityMap = new HashMap<String, Double>();
	static HashMap<String, Double> similarityScoreSumMap = new HashMap<String, Double>();
	static HashMap<String, Double> totalBySimSumSumMap = new HashMap<String, Double>();
	
	public static void main(String[] args) {
		convertJsonDataToMap();
		System.out.println("Please select user name from list: \n" + movieDataMap.keySet());
		String userName = new Scanner(System.in).nextLine();
		createSimilarityAndSimSumMap(userName);
		System.out.println("Recommendation for " + userName + " are " + totalBySimSumSumMap.keySet());
	}

	private static void createSimilarityAndSimSumMap(String userName) {
		HashMap<String, Double> userMovieDetail = movieDataMap.get(userName);
		
		for (Map.Entry<String, HashMap<String, Double>> movieDataContent : movieDataMap.entrySet()) {
			double similarityScore = 0.0;
			double sumX = 0;
			double sumXSquare = 0;
			double sumY = 0;
			double sumYSquare = 0;
			double sumXY = 0;
			if (!userName.equalsIgnoreCase(movieDataContent.getKey())) {
				int n = 0;
				for (Entry<String, Double> otherUserMovieDetail : movieDataContent.getValue().entrySet()) {
					String otherUserMovieName = otherUserMovieDetail.getKey();
					if (userMovieDetail.containsKey(otherUserMovieName)) {
						double userReview = userMovieDetail.get(otherUserMovieName).doubleValue();
						double otherUSerReview = otherUserMovieDetail.getValue().doubleValue();
						sumX += userReview;
						sumXSquare += userReview*userReview;
						sumY += otherUSerReview;
						sumYSquare += otherUSerReview*otherUSerReview;
						sumXY += userReview*otherUSerReview;
						n++;
					}
				}
				similarityScore = ((n*sumXY)-(sumX*sumY))/Math.sqrt((n*sumXSquare-sumX*sumX)*(n*sumYSquare-sumY*sumY));
				
				if (similarityScore > 0) {
					for (Entry<String, Double> otherUserMovieDetail : movieDataContent.getValue().entrySet()) {
						String otherUserMovieName = otherUserMovieDetail.getKey();
						if (!userMovieDetail.containsKey(otherUserMovieName)) {
							double otherUSerReview = otherUserMovieDetail.getValue().doubleValue();
							double exisitingSimilarityScoreSum = 0;
							double existingTotalBySimSumValue = 0;
							if (similarityScoreSumMap.get(otherUserMovieName) != null)
								exisitingSimilarityScoreSum = similarityScoreSumMap.get(otherUserMovieName);
							
							if (totalBySimSumSumMap.get(otherUserMovieName) != null)
								existingTotalBySimSumValue = totalBySimSumSumMap.get(otherUserMovieName);
							
							similarityScoreSumMap.put(otherUserMovieName, similarityScore + exisitingSimilarityScoreSum);
							totalBySimSumSumMap.put(otherUserMovieName, 
									(existingTotalBySimSumValue * exisitingSimilarityScoreSum + similarityScore * otherUSerReview)/
									(exisitingSimilarityScoreSum + similarityScore));
						}
					}
				}
				
			}
		}
		//System.out.println("similarityScoreSumMap : " + similarityScoreSumMap);
		//System.out.println("totalBySimSumSumMap : " + totalBySimSumSumMap);
	}

	private static HashMap<String, HashMap<String, Double>> convertJsonDataToMap() {
		try {
			//access the file
			FileInputStream jsonFileReader = new FileInputStream(JSON_FILE);
			
			JSONTokener jsonToken = new JSONTokener(jsonFileReader);
			
			//Create json object
			JSONObject jsonDataObject = new JSONObject(jsonToken);
			
			JSONArray recommList = (JSONArray) jsonDataObject.get(RecommConstant.DATA_SET);
			//populate map with user details and movies rating
			for (int i = 0; i < recommList.length(); i++) {
				JSONObject userDetails = recommList.getJSONObject(i);
				JSONArray movieListArray = (JSONArray) userDetails.get(RecommConstant.MOVIE_LIST);
				HashMap<String, Double> userMovieList = new HashMap<String, Double>();
				for (int j = 0; j < movieListArray.length(); j++) {
					userMovieList.put((String)movieListArray.getJSONObject(j).get(RecommConstant.MOVIE_NAME), 
							(Double)movieListArray.getJSONObject(j).get(RecommConstant.MOVIE_RATING));
				}
				
				movieDataMap.put((String)userDetails.get(RecommConstant.NAME), userMovieList);
			}
			//System.out.println(movieData);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return movieDataMap;
	}

}
