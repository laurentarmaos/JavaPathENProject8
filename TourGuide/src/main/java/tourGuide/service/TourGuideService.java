package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	public ExecutorService executorService;
	
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
	public VisitedLocation trackUserLocation(User user) {
		
		executorService = Executors.newFixedThreadPool(1);
		
		Future<VisitedLocation> futureVisitedLocation = executorService.submit(
				()-> gpsUtil.getUserLocation(user.getUserId())
				);
		
		VisitedLocation visitedLocation = null;
		try {
			visitedLocation = futureVisitedLocation.get();
		} catch (InterruptedException | ExecutionException e) {
			
			e.printStackTrace();
		}
		
		
		//VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		
		return nearbyAttractions;
	}
	
	
	
	
	
	

	// get the five closests attractions of user current position
	public List<Attraction> getFiveClosestAttractions(VisitedLocation visitedLocation){
		List<Attraction> fiveClosest = new ArrayList<>();
		List<Attraction> getAllAttractions = gpsUtil.getAttractions();
		
		executorService = Executors.newFixedThreadPool(1);
		
		Future<Attraction> futureClosestAttraction = executorService.submit(
	            ()-> getAllAttractions.get(0));
		Attraction closestAttraction = null;
		try {
			closestAttraction = futureClosestAttraction.get();
		} catch (InterruptedException | ExecutionException e) {
			
			e.printStackTrace();
		}
			
		//Attraction closestAttraction = getAllAttractions.get(0);
		double closest = rewardsService.getDistance(closestAttraction, visitedLocation.location);
		
		while(fiveClosest.size() < 5) {
			for(int i = 0; i < getAllAttractions.size(); i++) {
				if (rewardsService.getDistance(getAllAttractions.get(i), visitedLocation.location) < closest) {
					closestAttraction = getAllAttractions.get(i);
					closest = rewardsService.getDistance(closestAttraction, visitedLocation.location);
					
				}
			}
			fiveClosest.add(closestAttraction);
			getAllAttractions.remove(closestAttraction);
			closestAttraction = getAllAttractions.get(0);
			closest = rewardsService.getDistance(closestAttraction, visitedLocation.location);
		}
	
		return fiveClosest;
	}
	

	// get specifics informations of attractions
	public List<Object> getAttractionsInformations(Attraction attraction, VisitedLocation visitedLocation, User user){
		List<Object> attractionsInformations = new ArrayList<>();
		
		double attractionLatitude = rewardsService.getLatitude(attraction);
		double attractionLongitude = rewardsService.getLongitude(attraction);
		String attractionName = rewardsService.getAttractionName(attraction);
		double userLatitude = rewardsService.getLatitude(visitedLocation.location);
		double userLongitude = rewardsService.getLongitude(visitedLocation.location);
		double getDistance = rewardsService.getDistance(attraction, visitedLocation.location);
		int getReward = rewardsService.getRewardPoints(attraction, user);
		
		attractionsInformations.add(attractionLatitude);
		attractionsInformations.add(attractionLongitude);
		attractionsInformations.add(attractionName);
		attractionsInformations.add(userLatitude);
		attractionsInformations.add(userLongitude);
		attractionsInformations.add(getDistance);
		attractionsInformations.add(getReward);
		
		return attractionsInformations;
	}
	
	
	//get list of five closests attraction with the informations required
	public List<Object> getInformationsNearAttractions(VisitedLocation visitedLocation, User user) {
		List<Object> informationsNearAttractions = new ArrayList<>();
		
		for(int i = 0; i < getFiveClosestAttractions(visitedLocation).size(); i++) {
			informationsNearAttractions.add(getAttractionsInformations(getFiveClosestAttractions(visitedLocation).get(i), visitedLocation, user));
		}
		
		return informationsNearAttractions;
	}
	
	
	
	public void addUserLocation(User user, VisitedLocation visitedLocation){
		user.getVisitedLocations().add(visitedLocation);
	}

	public List<Object> getAllCurrentLocations(){
		List<Object> allCurrentLocations = new ArrayList<>();
		
		for(int i = 0; i < getAllUsers().size(); i++) {
			User user = getAllUsers().get(i);
			VisitedLocation visitedLocation = user.getLastVisitedLocation();
			
			double userLatitude = rewardsService.getLatitude(visitedLocation.location);
			double userLongitude = rewardsService.getLongitude(visitedLocation.location);
			
			List<Double> location = new ArrayList<>();
			location.add(userLatitude);
			location.add(userLongitude);
			
			List<Object> userLocation = new ArrayList<>();	
			userLocation.add(user.getUserId());			
			userLocation.add(location);
			
			allCurrentLocations.add(userLocation);
		}
		
		return allCurrentLocations;
	}
	
	
	
	
	
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
