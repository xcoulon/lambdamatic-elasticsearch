/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.lambdamatic.elasticsearch.search.SearchExpression;
import org.lambdamatic.internal.elasticsearch.search.QueryBuilderUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Publisher} a <code>SearchOperation</code> operation.
 * 
 * @param <QueryType> the Query type associated with the Domain Type to be searched.
 */
public class SearchPublisher<QueryType> implements Publisher<SearchResponse> {

  /** The usual Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchPublisher.class);

  /**
   * The Elasticsearch {@link Client}.
   */
  private final Client client;

  /**
   * The name of the index in which the <code>SearchOperation</code> operation will be performed.
   */
  private final String indexName;

  /** The type of document to get. */
  private final String type;

  /**
   * The {@link SearchExpression} to submit to the ES cluter.
   */
  private final SearchExpression<QueryType> searchExpression;

  /**
   * Constructor.
   * 
   * @param client the Elasticsearch {@link Client}
   * @param indexName the name of the index in which the <code>SearchOperation</code> operation will
   *        be performed.
   * @param type the type of document to get.
   * @param searchExpression the {@link SearchExpression} to submit to the ES cluter.
   */
  public SearchPublisher(final Client client, final String indexName, final String type,
      final SearchExpression<QueryType> searchExpression) {
    this.client = client;
    this.indexName = indexName;
    this.type = type;
    this.searchExpression = searchExpression;
  }

  @Override
  public void subscribe(final Subscriber<? super SearchResponse> subscriber) {
    final SearchRequestBuilder requestBuilder = this.client.prepareSearch(this.indexName)
        .setTypes(this.type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
    final QueryBuilder queryBuilder = QueryBuilderUtils.from(this.searchExpression);
    requestBuilder.setQuery(queryBuilder);
    final SearchSubscription subscription = new SearchSubscription(subscriber, requestBuilder);
    subscriber.onSubscribe(subscription);
  }

}
