package nextstep.subway.line.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private LineRepository lineRepository;
    private StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public LineResponse saveLine(LineRequest request) {
        Station upStation = stationService.findById(request.getUpStationId());
        Station downStation = stationService.findById(request.getDownStationId());
        Line persistLine = lineRepository.save(new Line(request.getName(), request.getColor(), upStation, downStation, request.getDistance(), request.getFare()));
        List<StationResponse> stations = getStationResponse(persistLine);
        return LineResponse.of(persistLine, stations);
    }

    public List<LineResponse> findLines() {
        List<Line> persistLines = findAll();
        return persistLines.stream()
                .map(line -> {
                    List<StationResponse> stations = getStationResponse(line);
                    return LineResponse.of(line, stations);
                })
                .collect(Collectors.toList());
    }

    public List<Line> findAll() {
        return lineRepository.findAll();
    }

    public Line findLineById(Long id) {
        return lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public LineResponse findLineResponseById(Long id) {
        Line persistLine = findLineById(id);
        List<StationResponse> stations = getStationResponse(persistLine);
        return LineResponse.of(persistLine, stations);
    }

    public void updateLine(Long id, LineRequest lineUpdateRequest) {
        Line persistLine = lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
        persistLine.update(new Line(lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long lineId, SectionRequest request) {
        Line line = findLineById(lineId);
        Station upStation = stationService.findStationById(request.getUpStationId());
        Station downStation = stationService.findStationById(request.getDownStationId());
        line.addLineStation(new Section(line, upStation, downStation, request.getDistance()));
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station delStation = stationService.findStationById(stationId);
        line.removeLineStation(delStation);
    }

    public List<StationResponse> getStationResponse(Line line) {
        return line.getStations()
                   .stream()
                   .map(StationResponse::of)
                   .collect(Collectors.toList());
    }
}
