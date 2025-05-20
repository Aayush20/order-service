package org.example.orderservice.clients;

import org.example.orderservice.dtos.UserProfileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserProfileClientTest {

    @Mock private RestTemplate restTemplate;
    @InjectMocks private UserProfileClient userProfileClient;

    private final String baseUrl = "http://auth-service";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userProfileClient = new UserProfileClient(restTemplate, baseUrl);
    }

    @Test
    void shouldReturnUserProfile_whenAuthServiceResponds() {
        String token = "Bearer xyz";
        String url = baseUrl + "/users/me";

        UserProfileDTO.AddressDTO address = new UserProfileDTO.AddressDTO();
        address.setCity("Delhi");
        address.setZipCode("110001");

        UserProfileDTO expectedProfile = new UserProfileDTO();
        expectedProfile.setEmail("test@example.com");
        expectedProfile.setAddress(address);

        ResponseEntity<UserProfileDTO> response = new ResponseEntity<>(expectedProfile, HttpStatus.OK);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserProfileDTO.class)))
                .thenReturn(response);

        UserProfileDTO actual = userProfileClient.getUserProfile(token);

        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo("test@example.com");
        assertThat(actual.getAddress().getCity()).isEqualTo("Delhi");
    }

    @Test
    void shouldThrowException_whenAuthServiceFails() {
        String token = "Bearer xyz";
        String url = baseUrl + "/users/me";

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserProfileDTO.class)))
                .thenThrow(new RuntimeException("Auth failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userProfileClient.getUserProfile(token));

        assertThat(ex.getMessage()).contains("Could not fetch user profile");
    }
}
