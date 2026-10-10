package foodcards.adapter.mapper;

import foodcards.adapter.models.AppAdapterTransEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AppAdapterTransMapper {
    void insert(AppAdapterTransEntity trans);
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    AppAdapterTransEntity findByRequestId(@Param("requestId") String requestId);

    int countByStatus(@Param("status") String status);
}
