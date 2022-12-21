package tourGuide;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {
	
	GpsUtil gpsUtil = mock(GpsUtil.class);
	RewardsService rewardsService = null;
	TourGuideService tourGuideService = null;
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		
		rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100000);
		tourGuideService = new TourGuideService(gpsUtil, rewardsService);
	}

	@Test
	public void userGetRewards() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//Attraction attraction = gpsUtil.getAttractions().get(0);
		Attraction attraction = new Attraction("attraction", "city", "state", 1, 1);
		
		List<Attraction> getAllAttractions = new ArrayList<>();
		getAllAttractions.add(attraction);
		
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(user.getLastVisitedLocation());
		when(gpsUtil.getAttractions()).thenReturn(getAllAttractions);
		
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertEquals(userRewards.size(), 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		//Attraction attraction = gpsUtil.getAttractions().get(0);
		Attraction attraction = new Attraction("attraction", "city", "state", 1, 1);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	//@Ignore // Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() {
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

//		InternalTestHelper.setInternalUserNumber(1);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction1 = new Attraction("attraction1", "city", "state", 1, 1);
		Attraction attraction2 = new Attraction("attraction2", "city", "state", 2, 2);
		
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction1, new Date()));
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction2, new Date()));
		List<Attraction> getAllAttractions = new ArrayList<>();
		getAllAttractions.add(attraction1);
		getAllAttractions.add(attraction2);
		
		when(gpsUtil.getAttractions()).thenReturn(getAllAttractions);
		
//		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
//		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = tourGuideService.getUserRewards(user);
		tourGuideService.tracker.stopTracking();

		assertEquals(getAllAttractions.size(), userRewards.size());
	}
	
}
