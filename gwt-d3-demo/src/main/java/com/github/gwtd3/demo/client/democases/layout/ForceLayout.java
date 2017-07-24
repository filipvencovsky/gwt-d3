/**
 * Copyright (c) 2013, Anthony Schiochet and Eric Citaire
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * The names Anthony Schiochet and Eric Citaire may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL BOSTOCK BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.gwtd3.demo.client.democases.layout;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.layout.Cluster;
import com.github.gwtd3.api.layout.Cluster.Node;
import com.github.gwtd3.api.layout.Force;
import com.github.gwtd3.api.layout.HierarchicalLayout;
import com.github.gwtd3.api.layout.HierarchicalLayout.Link;
import com.github.gwtd3.api.svg.Diagonal;
import com.github.gwtd3.demo.client.DemoCase;
import com.github.gwtd3.demo.client.Factory;
import com.github.gwtd3.demo.client.democases.layout.ClusterDendogram.FlareNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.logging.client.ConsoleLogHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;

public class ForceLayout extends FlowPanel implements DemoCase {

    private static final String JSON_URL = // GWT.getModuleBaseURL() +
    "demo-data/graph.json";
    private final MyResources css;
    private Force<GraphNode, GraphLink> force;
    private Selection svg;

    public interface Bundle extends ClientBundle {
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        @Source("ForceLayout.css")
        public MyResources css();
    }

    interface MyResources extends CssResource {
        String link();

        String node();
    }

    public ForceLayout() {
        css = Bundle.INSTANCE.css();
        css.ensureInjected();
    }
    
    public static class Graph extends JavaScriptObject {
    	protected Graph() {
    		
    	}
    	
    	public final native Array<GraphNode> nodes()/*-{
        	return this.nodes;
        }-*/;
    	
    	public final native Array<GraphLink> links()/*-{
    		return this.links;
    	}-*/;
    }

    public static class GraphNode extends JavaScriptObject {

        protected GraphNode() {

        }

        public final native String id()/*-{
        	return this.id;
        }-*/;

        public final native int group()/*-{
        	return this.group;
        }-*/;
    }
    
    public static class GraphLink extends JavaScriptObject {

        protected GraphLink() {

        }
        
        public final native String source()/*-{
    		return this.source;
    	}-*/;
        
        public final native String target()/*-{
			return this.target;
		}-*/;
        
        public final native int value()/*-{
    		return this.value;
    	}-*/;
        
    }

	@Override
    public void start() {
        int width = 960;
        final int height = 500;

        force = D3.layout().force();
       
        force
        	.size(height, width - 160)
        	.linkDistance(100)
        	.charge(-80)
        	.on(Force.ForceEventType.TICK, new DatumFunction<Force.Node<GraphNode>>() {
			      @Override
			      public Force.Node<GraphNode> apply(final Element context, final Value value, final int index) {
			    	  Selection node = svg.selectAll("." + css.node());
			    	  
			    	  node.attr("transform", new DatumFunction<String>() {
                          @Override
                          public String apply(final Element context, final Value value, final int index) {
                              return "translate(" + value.asCoords().x() + "," + value.asCoords().y() + ")";
                          }
                      });
			    	  
			    	  Selection link = svg.selectAll("." + css.link());
			    	  
			    	  link
			    	  	.attr("x1", new DatumFunction<String>() {
	                          @Override
	                          public String apply(final Element context, final Value value, final int index) {
	                              return String.valueOf(value.<Force.Link<GraphNode>> as().source().x());
	                          }
	                      })
			    	  	.attr("y1", new DatumFunction<String>() {
	                          @Override
	                          public String apply(final Element context, final Value value, final int index) {
	                              return String.valueOf(value.<Force.Link<GraphNode>> as().source().y());
	                          }
	                      })
			    	  	.attr("x2", new DatumFunction<String>() {
	                          @Override
	                          public String apply(final Element context, final Value value, final int index) {
	                              return String.valueOf(value.<Force.Link<GraphNode>> as().target().x());
	                          }
	                      })
			    	  	.attr("y2", new DatumFunction<String>() {
	                          @Override
	                          public String apply(final Element context, final Value value, final int index) {
	                              return String.valueOf(value.<Force.Link<GraphNode>> as().target().y());
	                          }
	                      });

			          return value.<Force.Node<GraphNode>> as();
			      }
			  }); //https://bl.ocks.org/mbostock/3750558

        svg = D3.select(this).append("svg")
                .attr("width", width)
                .attr("height", height)
                .append("g")
                .attr("transform", "translate(50,-150)");

        // Send request to server and catch any errors.
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, JSON_URL);

        try {
            Request request = builder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(final Request request, final Throwable exception) {
                    Window.alert("Couldn't retrieve JSON");
                }

                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    if (200 == response.getStatusCode()) {
                        Graph root = JsonUtils.safeEval(response.getText());                                               
                       
                        Array<Force.Node<GraphNode>> nodes = force.nodesFromData(root.nodes());
                        GWT.log(String.valueOf("nodes: "+nodes.length()));  
                        Array<Force.Link<GraphNode>> links = force.linksFromData(root.links());
                        GWT.log(String.valueOf("links: "+links.length()));
                        //TODO: nesou hrany informace?
                        //TODO: jak zobrazit informace z hran?
                        
                        force.start();

                        Selection link = svg.selectAll("." + css.link())
//                                .data(root.links())
                                .data(links)
                                .enter().append("line")                                
                                .attr("class", css.link());

                        Selection node = svg.selectAll("." + css.node())
                                .data(nodes)
                                .enter().append("g")
                                .attr("class", css.node())
                                .attr("r", 40);
//                                .on("dblclick", dblclick) //https://bl.ocks.org/mbostock/3750558 TODO: doubleclick
//                                .call(drag); //https://bl.ocks.org/mbostock/3750558 TODO: drag and drop

                        node.append("circle")
                                .attr("r", 4.5);

                        node.append("text")
                                .attr("dx", 8)
                                .attr("dy", 3)
                                .text(new DatumFunction<String>() {
                                    @Override
                                    public String apply(final Element context, final Value d, final int index) {
                                        return d.<Force.Node<GraphNode>> as().datum().id();
                                    }
                                });

                        D3.select(ForceLayout.this).select("svg").style("height", height + "px");
                        force.start();

                    } else {
                        Window.alert("Couldn't retrieve JSON (" + response.getStatusText()
                                + ")");
                    }
                }
            });
        } catch (RequestException e) {
            Window.alert("Couldn't retrieve JSON");
        }

    }

    @Override
    public void stop() {

    }

    public static Factory factory() {
        return new Factory() {
            @Override
            public DemoCase newInstance() {
                return new ForceLayout();
            }
        };
    }

}
