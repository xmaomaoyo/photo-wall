package com.mimi.photowall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 照片日期分组响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "照片日期分组")
public class PhotoDateGroupVO {

    @Schema(description = "拍摄日期")
    private LocalDate takenDate;

    @Schema(description = "该日期下的照片列表")
    private List<PhotoVO> photos;
}
