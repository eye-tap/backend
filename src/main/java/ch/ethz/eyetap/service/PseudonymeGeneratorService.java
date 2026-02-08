package ch.ethz.eyetap.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class PseudonymeGeneratorService {

    private static final List<String> ADJECTIVES = List.of(
            "Calm", "Bright", "Curious", "Gentle", "Quiet",
            "Swift", "Kind", "Nimble", "Brisk", "Mellow",
            "Clear", "Soft", "Lucky", "Happy", "Cool",
            "Sharp", "Smooth", "Steady", "Tiny", "Sunny"
    );

    private static final List<String> ANIMALS = List.of(
            "Otter", "Falcon", "Panda", "Koala", "Lynx",
            "Heron", "Dolphin", "Turtle", "Robin", "Badger",
            "Seal", "Moose", "Beaver", "Fox", "Hedgehog",
            "Sparrow", "Pelican", "Yak", "Alpaca", "Marten"
    );

    private final SecureRandom random = new SecureRandom();

    public String generatePseudonym() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        int number = random.nextInt(1000); // 0–999

        return adjective + animal + String.format("%03d", number);
    }
}
