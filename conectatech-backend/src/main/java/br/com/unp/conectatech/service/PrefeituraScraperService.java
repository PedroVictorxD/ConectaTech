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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrefeituraScraperService {

    private static final Logger log = LoggerFactory.getLogger(PrefeituraScraperService.class);
    private static final String BASE_URL = "https://paineldeempregos.prefeiturademossoro.com.br";
    private static final String VAGAS_URL = BASE_URL + "/vagas_por_cargos";
    private static final String EMPRESA = "Painel de Empregos - Prefeitura de Mossoró";
    private static final String LOCALIZACAO = "Mossoró, RN";

    private final VagaRepository vagaRepository;

    public PrefeituraScraperService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    public List<Vaga> importarVagas() {
        List<Vaga> vagasSalvas = new ArrayList<>();

        try {
            List<String> links = extrairLinksDetalhe();
            log.info("Prefeitura: {} cargos encontrados", links.size());

            for (String link : links) {
                try {
                    List<Vaga> vagas = scrapeDetalhe(link);
                    vagasSalvas.addAll(vagas);
                } catch (Exception e) {
                    log.warn("Erro ao scrape detalhe '{}': {}", link, e.getMessage());
                }
            }

            log.info("Prefeitura: {} vagas importadas no total", vagasSalvas.size());
        } catch (Exception e) {
            log.error("Erro ao importar vagas da Prefeitura: {}", e.getMessage(), e);
        }

        return vagasSalvas;
    }

    private List<String> extrairLinksDetalhe() throws Exception {
        Document doc = conectar(VAGAS_URL);
        log.info("Prefeitura: HTML title = '{}'", doc.title());
        Elements cards = doc.select("#page-content .card");
        log.info("Prefeitura: {} cards encontrados na pagina principal", cards.size());
        List<String> links = new ArrayList<>();

        for (Element card : cards) {
            Element linkEl = card.selectFirst("a.btn-success");
            if (linkEl != null) {
                String href = linkEl.attr("href");
                if (!href.isBlank()) {
                    links.add(BASE_URL + href);
                }
            }
        }

        return links;
    }

    private List<Vaga> scrapeDetalhe(String url) throws Exception {
        Document doc = conectar(url);
        Elements cards = doc.select("#page-content .card");
        List<Vaga> vagasSalvas = new ArrayList<>();

        for (Element card : cards) {
            Element tituloEl = card.selectFirst("h2.card-title");
            if (tituloEl == null) continue;

            String titulo = tituloEl.text().trim();

            Element descricaoEl = card.selectFirst("ul");
            String descricao = "";
            if (descricaoEl != null) {
                Elements items = descricaoEl.select("li");
                StringBuilder sb = new StringBuilder();
                for (Element li : items) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("- ").append(li.text().trim());
                }
                descricao = sb.toString();
            }

            Element candidatarEl = card.selectFirst("a.btn-success");
            String linkCandidatura = "";
            if (candidatarEl != null) {
                String href = candidatarEl.attr("href");
                linkCandidatura = href.startsWith("http") ? href : BASE_URL + href;
            }

            if (linkCandidatura.isBlank()) {
                linkCandidatura = url + "#" + titulo.hashCode();
            }

            if (!vagaRepository.existsByUrl(linkCandidatura)) {
                Vaga vaga = Vaga.builder()
                        .titulo(titulo)
                        .empresa(EMPRESA)
                        .descricao(descricao)
                        .localizacao(LOCALIZACAO)
                        .url(linkCandidatura)
                        .fonte(FonteVaga.PREFEITURA)
                        .build();

                vagasSalvas.add(vagaRepository.save(vaga));
                log.debug("Vaga salva: {}", titulo);
            }
        }

        return vagasSalvas;
    }

    private Document conectar(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(15000)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get();
    }
}
