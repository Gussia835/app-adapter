package foodcards.adapter.mapper;

import foodcards.adapter.models.GruVistaTabEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GruVistaTabMapper {
    /*
     Выбираем n записей со статусом WAIT
     */
    List<GruVistaTabEntity> selectWaitingRecords(@Param("n") int n);

    /*
    * Меняем статус на progress
    */
    void updateStatusToProgress(@Param("ids") List<Long> ids);

    /*
     * Меняем статус на error
     */
    void updateStatusToError(@Param("ids") List<Long> ids);

    /*
     * Меняем данные success
     */
    void updateDataSuccess(@Param("id") Long id,
                       @Param("newTbal") BigDecimal newTbal,
                       @Param("oldTbal") BigDecimal oldTbal);
}
