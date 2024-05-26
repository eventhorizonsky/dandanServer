package xyz.ezsky.tasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.ezsky.entity.dto.AnimeDTO;
import xyz.ezsky.dao.AnimeMapper;

import java.util.List;

@Component
public class AnimeScheduler {

    @Autowired
    private AnimeMapper animeMapper;

    @Scheduled(cron = "0 0 0 30 3,6,9,12 ?")// 每年的3月底、6月底、9月底、12月底执行一次
    public void updateIsAirToFalse() {
        animeMapper.updateAllAnimeIsAirToFalse();
    }
}
