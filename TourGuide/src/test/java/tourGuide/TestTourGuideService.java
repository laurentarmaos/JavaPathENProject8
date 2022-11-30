package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.intThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, attractions.size());
	}
	
	public void getTripDeals() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(10, providers.size());
	}
	
	
	@Test
	public void getFiveClosestAttractions() {
		Attraction attraction1 = new Attraction("attraction1", "city", "state", 1, 1);
		Attraction attraction2 = new Attraction("attraction2", "city", "state", 2, 2);
		Attraction attraction3 = new Attraction("attraction3", "city", "state", 4, 4);
		Attraction attraction4 = new Attraction("attraction4", "city", "state", 3, 3);
		Attraction attraction5 = new Attraction("attraction5", "city", "state", 15, 15);
		Attraction attraction6 = new Attraction("attraction6", "city", "state", 10, 10);
		
		List<Attraction> getAllAttractions = new ArrayList<>();
		getAllAttractions.add(attraction3);
		getAllAttractions.add(attraction1);
		getAllAttractions.add(attraction2);
		getAllAttractions.add(attraction6);
		getAllAttractions.add(attraction5);
		getAllAttractions.add(attraction4);
		
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		
		List<Attraction> fiveClosest = new ArrayList<>();
		Location userlocation = new Location(0, 0);
		VisitedLocation visitedLocation = new VisitedLocation(null, userlocation, null);
		
		Attraction closestAttraction = getAllAttractions.get(0);
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
		
		
		for(int i = 0; i<fiveClosest.size(); i++) {
			System.out.println(fiveClosest.get(i).attractionName);
		}
		
		assertEquals(5, fiveClosest.size());
		assertEquals(attraction1, fiveClosest.get(0));
		assertEquals(attraction2, fiveClosest.get(1));
		assertEquals(attraction4, fiveClosest.get(2));
		assertEquals(attraction3, fiveClosest.get(3));
		assertEquals(attraction6, fiveClosest.get(4));

	}
	
	
	@Test
	public void getAttractionsInformations() {
		Attraction attraction = new Attraction("attractionName", "city", "state", 1, 1);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(0, 0), null);
		
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		
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
		
		
		System.out.println(attractionsInformations);
		
		assertEquals(1.0, attractionsInformations.get(0));
		assertEquals(1.0, attractionsInformations.get(1));
		assertEquals("attractionName", attractionsInformations.get(2));
		assertEquals(0.0, attractionsInformations.get(3));
		assertEquals(0.0, attractionsInformations.get(4));
		//assertEquals(null, attractionsInformations.get(5));
		//assertEquals(null, attractionsInformations.get(6));
	}
	
	
	@Test
	public void getInformationsNearAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(0, 0), null);
		
		List<Object> informationsNearAttractions = new ArrayList<>();
		
		for(int i = 0; i < tourGuideService.getFiveClosestAttractions(visitedLocation).size(); i++) {
			informationsNearAttractions.add(tourGuideService.getAttractionsInformations(tourGuideService.getFiveClosestAttractions(visitedLocation).get(i), visitedLocation, user));
		}
		
		System.out.println(informationsNearAttractions);
	}
	
}
