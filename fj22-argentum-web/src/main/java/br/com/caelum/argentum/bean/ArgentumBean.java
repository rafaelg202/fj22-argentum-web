package br.com.caelum.argentum.bean;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.chart.ChartModel;

import br.com.caelum.argentum.grafico.GeradorModeloGrafico;
import br.com.caelum.argentum.indicadores.Indicador;
import br.com.caelum.argentum.indicadores.IndicadorFechamento;
import br.com.caelum.argentum.indicadores.MediaMovelSimples;
import br.com.caelum.argentum.modelo.Candlestick;
import br.com.caelum.argentum.modelo.CandlestickFactory;
import br.com.caelum.argentum.modelo.Negociacao;
import br.com.caelum.argentum.modelo.SerieTemporal;
import br.com.caelum.argentum.ws.ClienteWebService;

@ManagedBean
@ViewScoped
public class ArgentumBean implements Serializable{
	private List<Negociacao> negociacoes;
	private ChartModel modeloGrafico;
	private String nomeMedia;
	public String getNomeMedia() {
		return nomeMedia;
	}

	public void setNomeMedia(String nomeMedia) {
		this.nomeMedia = nomeMedia;
	}

	public String getNomeIndicadorBase() {
		return nomeIndicadorBase;
	}

	public void setNomeIndicadorBase(String nomeIndicadorBase) {
		this.nomeIndicadorBase = nomeIndicadorBase;
	}

	private String nomeIndicadorBase;
	
	public ArgentumBean(){
		geraGrafico();
	}

	public void geraGrafico() {
		System.out.println("PLOTANDO: " + nomeMedia + " de " + nomeIndicadorBase);
		this.negociacoes = new ClienteWebService().getNegociacoes();
		List<Candlestick> candles = new CandlestickFactory().constroiCandles(negociacoes);
		SerieTemporal serie  = new SerieTemporal(candles);
		GeradorModeloGrafico geradorGrafico =
				new GeradorModeloGrafico(serie, 2, serie.getUltimaPosicao());
		geradorGrafico.plotaIndicador(defineIndicador());
		this.modeloGrafico = geradorGrafico.getModeloGrafico();
	}
	
	private Indicador defineIndicador() {
		if(nomeIndicadorBase == null || nomeMedia == null){
			return new MediaMovelSimples(new IndicadorFechamento());
		}
		try{
		String pacote = "br.com.caelum.argentum.indicadores.";
		Class<?> classeIndicadorBase = Class.forName(pacote + nomeIndicadorBase);
		Indicador indicadorBase = (Indicador) classeIndicadorBase.newInstance();
		
		Class<?> classeMedia = Class.forName(pacote + nomeMedia);
		Constructor<?> construtorMedia = classeMedia.getConstructor(Indicador.class);
		Indicador indicador = (Indicador) construtorMedia.newInstance(indicadorBase);
		return indicador;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	public List<Negociacao> getNegociacoes(){
		return negociacoes;
	}
	
	public ChartModel getModeloGrafico() {
		return modeloGrafico;
	}
}
