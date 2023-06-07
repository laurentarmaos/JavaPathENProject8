package tourGuide;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.TripPricer;

@ExtendWith(MockitoExtension.class)
public class TestRewardsService {
	
	GpsUtil gpsUtil = mock(GpsUtil.class);
	
	TripPricer tripPricer = mock(TripPricer.class);
	@InjectMocks	
	RewardsService rewardsService;
	
	@InjectMocks
	TourGuideService tourGuideService;
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		
		rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		tourGuideService = new TourGuideService(gpsUtil, rewardsService);
	}

	//@Ignore
	@Test
	public void userGetRewards() {
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		Attraction attraction = new Attraction("attraction", "city", "state", 1, 1);
		Attraction attraction2 = new Attraction("attraction2", "city", "state", 2, 2);
		
		List<Attraction> getAllAttractions = new ArrayList<>();
		getAllAttractions.add(attraction);
		getAllAttractions.add(attraction2);
		
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction2, new Date()));
		
		//when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(user.getLastVisitedLocation());
		when(gpsUtil.getAttractions()).thenReturn(getAllAttractions);
		
		rewardsService.calculateRewards(user);
		
		List<UserReward> userRewards = rewardsService.calculateRewards(user);

		tourGuideService.tracker.stopTracking();
		
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		assertEquals(userRewards.size(), getAllAttractions.size());
		System.out.println(getAllAttractions.size());
		System.out.println(userRewards.size());
	}
	
	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = new Attraction("attraction", "city", "state", 1, 1);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
}
