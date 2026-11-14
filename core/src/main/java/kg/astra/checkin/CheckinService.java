package kg.astra.checkin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CheckinService {
    private final CheckinRepository checkinRepository;

    public CheckinResponse submit(CheckinRequest request){
        Checkin checkin = Checkin.from(request);
        checkinRepository.save(checkin);
        return new CheckinResponse(checkin.getId(), null);
    }
}
