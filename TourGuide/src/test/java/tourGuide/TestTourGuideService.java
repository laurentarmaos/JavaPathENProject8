package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


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
import tripPricer.TripPricer;


@ExtendWith(MockitoExtension.class)
public class TestTourGuideService {

	GpsUtil gpsUtil = mock(GpsUtil.class);

	TripPricer tripPricer = mock(TripPricer.class);

	RewardsService rewardsService;
	
	@InjectMocks
	TourGuideService tourGuideService;
	
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		
		rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		tourGuideService = new TourGuideService(gpsUtil, rewardsService);
	}


	@Test
	public void testGetUserLocation() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		
		Attraction attraction = new Attraction("attraction1", "city", "state", 1, 1);	
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		
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
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(new VisitedLocation(user.getUserId(), null, null));

		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
		
	
	//@Ignore
	@Test
	public void getTripDeals() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		//tourGuideService.tracker.stopTracking();

		assertNotNull(providers.get(0));
        assertNotNull(providers);
        assertNotEquals(0, providers.size());
        assertNotNull(user.getTripDeals());
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
		
		
		Location userlocation = new Location(0, 0);
		VisitedLocation visitedLocation = new VisitedLocation(null, userlocation, null);
		when(gpsUtil.getAttractions()).thenReturn(getAllAttractions);
		List<Attraction> fiveClosest = tourGuideService.getFiveClosestAttractions(visitedLocation);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, fiveClosest.size());
		assertEquals(attraction1, fiveClosest.get(0));
		assertEquals(attraction2, fiveClosest.get(1));
		assertEquals(attraction4, fiveClosest.get(2));
		assertEquals(attraction3, fiveClosest.get(3));
		assertEquals(attraction6, fiveClosest.get(4));
		
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
//		InternalTestHelper.setInternalUserNumber(10000);
//
//		
//		Location userlocation = new Location(0, 0);
//		VisitedLocation visitedLocation = new VisitedLocation(null, userlocation, null);
//		List<Attraction> list = tourGuideService.getFiveClosestAttractions(visitedLocation);
//		System.out.println(list);
//		assertEquals(5, list.size());

	}
	
	
	@Test
	public void getAttractionsInformations() {
		Attraction attraction = new Attraction("attractionName", "city", "state", 1, 1);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(0, 0), null);
		
		
		assertEquals(1.0, tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(0));
		assertEquals(1.0, tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(1));
		assertEquals("attractionName", tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(2));
		assertEquals(0.0, tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(3));
		assertEquals(0.0, tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(4));
		assertEquals(97.64439545235415, tourGuideService.getAttractionsInformations(attraction, visitedLocation, user).get(5));
	}
	
	
	@Test
	public void getInformationsNearAttractions() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(0, 0), null);
		
		
		
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

		when(gpsUtil.getAttractions()).thenReturn(getAllAttractions);
		
		List<Object> informationsNearAttractions = tourGuideService.getInformationsNearAttractions(visitedLocation, user);
		
	
		tourGuideService.tracker.stopTracking();
		assertEquals(5, informationsNearAttractions.size());
	}
	

	
	@Test
	public void getAllCurrentLocations() {
		
		User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);
	
		VisitedLocation user1visitedLocation1 = new VisitedLocation(user1.getUserId(), new Location(0, 0), null);
		VisitedLocation user1visitedLocation2 = new VisitedLocation(user1.getUserId(), new Location(1, 1), null);
		user1.getVisitedLocations().add(user1visitedLocation1);
		user1.getVisitedLocations().add(user1visitedLocation2);
		
		VisitedLocation user2visitedLocation1 = new VisitedLocation(user2.getUserId(), new Location(0, 0), null);
		VisitedLocation user2visitedLocation2 = new VisitedLocation(user2.getUserId(), new Location(2, 2), null);
		VisitedLocation user2visitedLocation3 = new VisitedLocation(user2.getUserId(), new Location(3, 3), null);
		user2.getVisitedLocations().add(user2visitedLocation1);
		user2.getVisitedLocations().add(user2visitedLocation2);
		user2.getVisitedLocations().add(user2visitedLocation3);
		

		Map<String, Location> loc = tourGuideService.getAllCurrentLocations();

		
		tourGuideService.tracker.stopTracking();
		assertEquals(loc.size(), InternalTestHelper.getInternalUserNumber()+2);
	}
	
}
