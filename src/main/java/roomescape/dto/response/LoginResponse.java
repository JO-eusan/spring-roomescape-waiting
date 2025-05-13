package roomescape.dto.response;

import roomescape.domain.Member;

public record LoginResponse(
    String name) {

    public static LoginResponse from(Member member) {
        return new LoginResponse(member.getName());
    }
}
