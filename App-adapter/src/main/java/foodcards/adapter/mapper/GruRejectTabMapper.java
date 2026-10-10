package foodcards.adapter.mapper;

import foodcards.adapter.models.GruRejectTabEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GruRejectTabMapper {
    void insert(GruRejectTabEntity rejectTab);
}
