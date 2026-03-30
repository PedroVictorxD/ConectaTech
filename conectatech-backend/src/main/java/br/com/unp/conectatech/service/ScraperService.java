package br.com.unp.conectatech.service;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.VagaRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private final VagaRepository vagaRepository;

    @Value("${scraper.enabled}")
    private boolean scraperEnabled;

    @Value("${scraper.target.url}")
    private String targetUrl;

    @Value("${scraper.target.card-selector}")
    private String cardSelector;

    @Value("${scraper.target.title-selector}")
    private String titleSelector;

    @Value("${scraper.target.company-selector}")
    private String companySelector;

    @Value("${scraper.target.description-selector:p}")
    private String descriptionSelector;

    @Value("${scraper.target.location-selector}")
    private String locationSelector;

    @Value("${scraper.target.link-selector:a}")
    private String linkSelector;

    @Value("${scraper.target.cidade:Mossoró}")
    private String cidadePadrao;

    public ScraperService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Scheduled(cron = "${scraper.cron}")
    public void executarScrapingAgendado() {
        if (!scraperEnabled) {
            log.info("Scraper desabilitado. Pulando execução agendada.");
            return;
        }
        log.info("Iniciando scraping agendado...");
        List<Vaga> vagas = executarScraping();
        log.info("Scraping concluído. {} vagas importadas.", vagas.size());
    }

    public List<Vaga> executarScraping() {
        List<Vaga> vagasSalvas = new ArrayList<>();

        try {
            log.info("Scraping URL: {}", targetUrl);

            Document doc = Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            Elements vagaElements = doc.select(cardSelector);
            log.info("Encontrados {} cards com seletor '{}'", vagaElements.size(), cardSelector);

            if (vagaElements.isEmpty()) {
                log.warn("Nenhum card encontrado. Verifique se os seletores CSS estão corretos para o site alvo.");
                log.debug("HTML title: {}", doc.title());
                String bodyText = doc.body() != null ? doc.body().text() : "";
                log.debug("HTML body preview: {}", bodyText.substring(0, Math.min(500, bodyText.length())));
                return vagasSalvas;
            }

            for (Element element : vagaElements) {
                String titulo = extractText(element, titleSelector);
                String empresa = extractText(element, companySelector);
                String descricao = extractText(element, descriptionSelector);
                String localizacao = extractText(element, locationSelector);
                String link = extractLink(element, linkSelector);

                if (titulo.isEmpty()) {
                    log.debug("Card ignorado: título vazio");
                    continue;
                }

                if (link.isEmpty()) {
                    link = targetUrl + "#" + titulo.hashCode();
                }

                if (!vagaRepository.existsByUrl(link)) {
                    Vaga vaga = Vaga.builder()
                            .titulo(titulo)
                            .empresa(empresa.isEmpty() ? "Empresa não informada" : empresa)
                            .descricao(descricao)
                            .localizacao(localizacao.isEmpty() ? cidadePadrao : localizacao)
                            .url(link)
                            .fonte(FonteVaga.JSOUP)
                            .build();

                    vagasSalvas.add(vagaRepository.save(vaga));
                    log.debug("Vaga salva: {}", titulo);
                }
            }

            log.info("Scraping finalizado: {} novas vagas salvas de {} cards encontrados",
                    vagasSalvas.size(), vagaElements.size());

        } catch (Exception e) {
            log.error("Erro ao fazer scraping de '{}': {}", targetUrl, e.getMessage(), e);
        }

        return vagasSalvas;
    }

    private String extractText(Element parent, String selectors) {
        for (String selector : selectors.split(",")) {
            Element el = parent.selectFirst(selector.trim());
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return "";
    }

    private String extractLink(Element parent, String selector) {
        Element link = parent.selectFirst(selector);
        if (link != null) {
            String href = link.absUrl("href");
            return href.isEmpty() ? link.attr("href") : href;
        }
        return "";
    }
}
