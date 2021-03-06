/*
 *  Copyright (c) 2012, 2013, Credit Suisse (Anatole Tresch), Werner Keil.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * Contributors:
 *    Anatole Tresch - initial implementation
 *    Wernner Keil - extensions and adaptions.
 */
package net.java.javamoney.ri.ext.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.inject.Singleton;
import javax.money.CurrencyUnit;
import javax.money.ext.Region;

import net.java.javamoney.ri.ext.spi.RegionalCurrencyUnitProviderSpi;

/**
 * This class models the singleton defined by JSR 354 that provides accessors
 * for {@link CurrencyUnit}.
 * 
 * @author Anatole Tresch
 */
public final class RegionalCurrencyUnitProvider {

	/** Loaded region providers. */
	private List<RegionalCurrencyUnitProviderSpi> regionalCurrencyProviders = new ArrayList<RegionalCurrencyUnitProviderSpi>();

	/**
	 * Constructor, loads the spi components.
	 */
	public RegionalCurrencyUnitProvider() {
		reload();
	}

	/**
	 * This method reloads the providers available from the
	 * {@link ServiceLoader}. This adds providers that were not yet visible
	 * before.
	 */
	@SuppressWarnings("unchecked")
	public void reload() {
		for (RegionalCurrencyUnitProviderSpi provSPI : ServiceLoader
				.load(RegionalCurrencyUnitProviderSpi.class)) {
			if (!regionalCurrencyProviders.contains(provSPI)) {
				this.regionalCurrencyProviders.add(provSPI);
			}
		}
	}

	public Set<CurrencyUnit> getAll(Region region, Long timestamp) {
		Set<CurrencyUnit> result = new HashSet<CurrencyUnit>();
		for (RegionalCurrencyUnitProviderSpi prov : regionalCurrencyProviders) {
			Collection<CurrencyUnit> currencies = prov.getAll(region);
			if (currencies != null) {
				result.addAll(currencies);
			}
		}
		return result;
	}

	public boolean isLegalCurrencyUnit(CurrencyUnit currency, Region region,
			Long timestamp) {
		Set<CurrencyUnit> tenders = getLegalCurrencyUnits(region, timestamp);
		for (CurrencyUnit currencyUnit : tenders) {
			if (!currencyUnit.getNamespace().equals(currency.getNamespace())) {
				continue;
			}
			if (!currencyUnit.getCurrencyCode().equals(
					currency.getCurrencyCode())) {
				continue;
			}
			return true;
		}
		return false;
	}

	public Set<CurrencyUnit> getLegalCurrencyUnits(Region region, Long timestamp) {
		Set<CurrencyUnit> result = new HashSet<CurrencyUnit>();
		for (RegionalCurrencyUnitProviderSpi prov : regionalCurrencyProviders) {
			Collection<CurrencyUnit> currencies = prov.getLegalTenders(region,
					timestamp);
			if (currencies != null) {
				result.addAll(currencies);
			}
		}
		return result;
	}
}
