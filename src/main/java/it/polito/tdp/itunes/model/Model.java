package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

public class Model {
	
	private Graph<Album, DefaultWeightedEdge> grafo;
	private Map<Integer, Album> identity_map;
	private ItunesDAO dao;

	public Model() {
		super();
		this.dao = new ItunesDAO();
	}
	
	public void crea_grafo(Integer n) {
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		this.identity_map = new HashMap<>();
		
		
		dao.getAllAlbums(identity_map, n);
		
		Graphs.addAllVertices(this.grafo, this.identity_map.values());
		
		for(Album a1: this.identity_map.values()) {
			for(Album a2: this.identity_map.values()) {
				if(a1.equals(a2)) {
					continue;
				}
				if(this.grafo.containsVertex(a1) && this.grafo.containsVertex(a2)) {
					if(this.grafo.containsEdge(a2, a1) || this.grafo.containsEdge(a1, a2)) {
						continue;
					}
					
					//calcolo il peso dell'arco
					Integer peso = a1.getNumber_track() - a2.getNumber_track();
					if(peso > 0) {
						//a1 > a2
						//l'arco parte da a2 con meno canzoni ad a1 con piu canzoni
						Graphs.addEdge(this.grafo, a2, a1, peso);
					} else if (peso < 0) {
						//a2 > a1
						peso = -peso;
						Graphs.addEdge(this.grafo, a1, a2, peso);
					}
					
				}
			}
		}
		
		System.out.println(this.grafo.vertexSet().size());
		System.out.println(this.grafo.edgeSet().size());
		
		
	}

	public List<Album> getAllAbumsInGrafo() {
		// TODO Auto-generated method stub
		List<Album> albums = new ArrayList<>(this.identity_map.values());
		Collections.sort(albums);
		return albums;
	}
	
	
	public void calcola_bilanci() {
		
		for(Album d: this.grafo.vertexSet()) {
			//somma entranti - somma uscenti
			Integer bilancio_entranti = 0;
			for(DefaultWeightedEdge e: this.grafo.incomingEdgesOf(d)) {
				bilancio_entranti += (int) this.grafo.getEdgeWeight(e);
			}
			Integer bilancio_uscenti = 0;
			for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(d)) {
				bilancio_uscenti += (int) this.grafo.getEdgeWeight(e);
			}
			Integer bilancio = bilancio_entranti - bilancio_uscenti;
			d.setBilancio(bilancio);
		}
		
	}
	
	public List<Album> get_successori(Album a1) {
		calcola_bilanci();
		Set<DefaultWeightedEdge> archi = this.grafo.outgoingEdgesOf(a1);
		List<Album> result = new ArrayList<>();
		
		for(DefaultWeightedEdge e : archi) {
			result.add(this.grafo.getEdgeTarget(e));
		}
		
		Collections.sort(result, new Comparator<Album>() {
			@Override
			public int compare(Album o1, Album o2) {
				// TODO Auto-generated method stub
				return -o1.getBilancio().compareTo(o2.getBilancio());
			}
		});
		
		return result;
	}
	
	
	
}






