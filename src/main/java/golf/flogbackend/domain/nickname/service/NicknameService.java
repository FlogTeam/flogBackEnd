package golf.flogbackend.domain.nickname.service;

import golf.flogbackend.domain.member.repository.MemberRepository;
import golf.flogbackend.domain.nickname.entity.Adjective;
import golf.flogbackend.domain.nickname.entity.Noun;
import golf.flogbackend.domain.nickname.repository.AdjectiveRepository;
import golf.flogbackend.domain.nickname.repository.NounRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class NicknameService {
    private final MemberRepository memberRepository;
    private final AdjectiveRepository adjectiveRepository;
    private final NounRepository nounRepository;

    public String getRandomNickname() {
        Random random = new Random();
        String nickname;
        do {
            Long randomIndexA = random.nextLong(277) + 1;
            Long randomIndexN = random.nextLong(97) + 1;
            String adjective = adjectiveRepository.findById(randomIndexA).map(Adjective::getText).orElse("");
            String noun = nounRepository.findById(randomIndexN).map(Noun::getText).orElse("");
            nickname = adjective + " " + noun + (random.nextInt(98) + 1);
        } while (memberRepository.existsByNickname(nickname) || StringUtils.isEmptyOrWhitespace(nickname));

        return nickname;
    }
}
