package foodcards.adapter.mapper;

import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppAdapterIoMsgsMapper {
    void insert(AppAdapterIoMsgsEntity ioMsgs);
}
