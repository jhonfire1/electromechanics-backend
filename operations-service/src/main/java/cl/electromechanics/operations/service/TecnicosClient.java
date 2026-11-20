package cl.electromechanics.operations.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@Component
public class TecnicosClient {
    private final RestClient cliente;

    public TecnicosClient(@Value("${app.auth-service-url}") String url) {
        SimpleClientHttpRequestFactory solicitudes = new SimpleClientHttpRequestFactory();
        solicitudes.setConnectTimeout(Duration.ofSeconds(5));
        solicitudes.setReadTimeout(Duration.ofSeconds(5));
        cliente = RestClient.builder().baseUrl(url).requestFactory(solicitudes).build();
    }

    public void validarAsignacion(Long tecnicoId, String authorization) {
        try {
            cliente.get().uri("/api/auth/tecnicos/{id}", tecnicoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El técnico seleccionado no existe");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o vencida");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Asignación no permitida");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo comprobar el técnico");
        }
    }
}
