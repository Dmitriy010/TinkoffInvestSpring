package dto;

import com.dmitriy.springinvesttinkoff.models.MyPortfolio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDto {
    List<MyPortfolio> portfolioDto;
}
