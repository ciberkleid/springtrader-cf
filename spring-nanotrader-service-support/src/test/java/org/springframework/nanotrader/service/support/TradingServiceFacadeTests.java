/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.nanotrader.service.support;

import org.dozer.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.nanotrader.data.domain.Accountprofile;
import org.springframework.nanotrader.data.domain.test.OrderDataOnDemand;
import org.springframework.nanotrader.data.service.AccountProfileService;
import org.springframework.nanotrader.service.FallBackAccountProfileService;
import org.springframework.nanotrader.data.service.QuoteService;
import org.springframework.nanotrader.data.service.TradingService;
import org.springframework.nanotrader.service.domain.Order;
import org.springframework.nanotrader.service.domain.Quote;
import org.springframework.nanotrader.service.support.TradingServiceFacadeImpl.OrderGateway;
import org.springframework.nanotrader.service.support.config.IntegrationTestConfig;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Gary Russell
 *
 */
@ActiveProfiles("test")
@ContextConfiguration(classes = { IntegrationTestConfig.class }, loader = AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TradingServiceFacadeTests {

	@Autowired
	private OrderDataOnDemand orderDataOnDemand;

	@Autowired
	private TradingServiceFacade tradingServiceFacade;

	@Autowired
	@Qualifier( "rtQuoteService")
	private QuoteService quoteService;

	@Autowired
	private OrderGateway orderGateway;

	@Autowired
	private AccountProfileService accountProfileService;

	@Autowired
    private Mapper mapper;

	private Accountprofile profile;

	@Before
	public void setupMocks() {
		Mockito.doNothing().when(orderGateway).sendOrder(new Order());
		profile = accountProfileService.saveAccountProfile(FallBackAccountProfileService.fakeAccountProfile(true));
	}

	@Test
	public void testSynch() {
		org.springframework.nanotrader.data.domain.Order existingOrder = 
				orderDataOnDemand.getRandomOrder();
		assertNotNull(existingOrder.getQuoteid());
		Order orderRequest = new Order();
		orderRequest.setAccountid(profile.getAccounts().get(0).getAccountid());
		orderRequest.setOrdertype(TradingService.ORDER_TYPE_BUY);
		Quote quote = new Quote();
		mapper.map(quoteService.findBySymbol("GOOG"), quote);
		orderRequest.setQuote(quote);
		assertNotNull(orderRequest.getQuote().getQuoteid());
		orderRequest.setQuantity(BigDecimal.valueOf(100));
		Long id = tradingServiceFacade.saveOrder(orderRequest, true);
		assertNotNull(id);
	}

	@Test
	public void testASynch() {
		org.springframework.nanotrader.data.domain.Order existingOrder = 
				orderDataOnDemand.getRandomOrder();
		Order orderRequest = new Order();
		orderRequest.setAccountid(existingOrder.getAccountid());
		orderRequest.setOrdertype(TradingService.ORDER_TYPE_BUY);
		Quote quote = new Quote();
		quote.setSymbol("GOOG");
		orderRequest.setQuote(quote);
		orderRequest.setQuantity(BigDecimal.valueOf(100));
		Long id = tradingServiceFacade.saveOrder(orderRequest, false);
		assertNull(id);
	}

}
