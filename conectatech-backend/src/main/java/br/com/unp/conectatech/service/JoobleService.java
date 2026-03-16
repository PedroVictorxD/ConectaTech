package br.com.unp.conectatech.service;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.VagaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class JoobleService {

    private static final Logger log = LoggerFactory.getLogger(JoobleService.class);

    @Value("${jooble.api.url}")
    private String apiUrl;

    @Value("${jooble.api.key}")
    private String apiKey;

    private final VagaRepository vagaRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public JoobleService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<Vaga> buscarVagasJooble(String keywords, String location) {
        List<Vaga> vagasSalvas = new ArrayList<>();

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("SUA_CHAVE_JOOBLE_AQUI")) {
            log.warn("Jooble API key não configurada. Configure 'jooble.api.key' no application.properties ou a variável de ambiente JOOBLE_API_KEY.");
            return vagasSalvas;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new JoobleRequest(keywords, location));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode jobs = root.get("jobs");

                if (jobs != null && jobs.isArray()) {
                    for (JsonNode job : jobs) {
                        String url = job.has("link") ? job.get("link").asText() : "";

                        if (!vagaRepository.existsByUrl(url)) {
                            Vaga vaga = Vaga.builder()
                                    .titulo(job.has("title") ? job.get("title").asText() : "Sem título")
                                    .empresa(job.has("company") ? job.get("company").asText() : "Empresa não informada")
                                    .descricao(job.has("snippet") ? job.get("snippet").asText() : "")
                                    .localizacao(job.has("location") ? job.get("location").asText() : location)
                                    .url(url)
                                    .fonte(FonteVaga.JOOBLE)
                                    .dataPublicacao(parseDate(job.has("updated") ? job.get("updated").asText() : null))
                                    .build();

                            vagasSalvas.add(vagaRepository.save(vaga));
                        }
                    }
                }

                log.info("Jooble: {} vagas importadas para keywords='{}', location='{}'",
                        vagasSalvas.size(), keywords, location);
            } else {
                log.warn("Jooble API retornou status {}", response.statusCode());
            }

        } catch (Exception e) {
            log.error("Erro ao buscar vagas do Jooble: {}", e.getMessage(), e);
        }

        return vagasSalvas;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * DTO interno para o body da requisição Jooble.
     */
    private record JoobleRequest(String keywords, String location) {
    }
}
