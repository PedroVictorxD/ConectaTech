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

    public ScraperService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    /**
     * Execução agendada via cron (padrão: diariamente às 6h).
     * Configurável por scraper.cron no application.properties.
     */
    @Scheduled(cron = "${scraper.cron}")
    public void executarScrapingAgendado() {
        if (!scraperEnabled) {
            log.info("Scraper desabilitado. Pulando execução agendada.");
            return;
        }

        log.info("Iniciando scraping agendado...");

        // Exemplo: scraping do CIEE para vagas de estágio
        List<Vaga> vagas = scrapeCiee("Mossoró");
        log.info("Scraping concluído. {} vagas importadas.", vagas.size());
    }

    /**
     * Scraping de vagas do CIEE (exemplo de implementação).
     * Adapte os seletores CSS conforme a estrutura real do site alvo.
     */
    public List<Vaga> scrapeCiee(String cidade) {
        List<Vaga> vagasSalvas = new ArrayList<>();
        String url = "https://portal.ciee.org.br/estudantes/vagas/?cidade=" + cidade;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // Seletores CSS genéricos — devem ser adaptados ao site real
            Elements vagaElements = doc.select(".vaga-card, .job-listing, .vacancy-item, article.vaga");

            for (Element element : vagaElements) {
                String titulo = extractText(element, ".titulo, .job-title, h2, h3");
                String empresa = extractText(element, ".empresa, .company-name, .employer");
                String descricao = extractText(element, ".descricao, .job-description, .snippet, p");
                String localizacao = extractText(element, ".localizacao, .location, .city");
                String link = extractLink(element, "a");

                if (titulo.isEmpty() || link.isEmpty())
                    continue;

                if (!vagaRepository.existsByUrl(link)) {
                    Vaga vaga = Vaga.builder()
                            .titulo(titulo)
                            .empresa(empresa.isEmpty() ? "Empresa não informada" : empresa)
                            .descricao(descricao)
                            .localizacao(localizacao.isEmpty() ? cidade : localizacao)
                            .url(link)
                            .fonte(FonteVaga.JSOUP)
                            .build();

                    vagasSalvas.add(vagaRepository.save(vaga));
                }
            }

            log.info("CIEE: {} vagas encontradas para cidade='{}'", vagasSalvas.size(), cidade);

        } catch (Exception e) {
            log.error("Erro ao fazer scraping do CIEE: {}", e.getMessage(), e);
        }

        return vagasSalvas;
    }

    /**
     * Extrai texto do primeiro elemento que corresponda aos seletores CSS.
     */
    private String extractText(Element parent, String selectors) {
        for (String selector : selectors.split(",")) {
            Element el = parent.selectFirst(selector.trim());
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return "";
    }

    /**
     * Extrai URL do primeiro link encontrado no elemento.
     */
    private String extractLink(Element parent, String selector) {
        Element link = parent.selectFirst(selector);
        if (link != null) {
            String href = link.absUrl("href");
            return href.isEmpty() ? link.attr("href") : href;
        }
        return "";
    }
}
