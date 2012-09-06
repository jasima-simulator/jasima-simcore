/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * 
 */
package jasima.core.util.observer;

/**
 * Simplified version of the Observer-pattern using Java Generics.
 * 
 * used as a reference: http://forum.java.sun.com/thread.jspa?threadID=576544
 * 2008-04-13
 */
public interface Notifier<N extends Notifier<N, E>, E> {
	public void addNotifierListener(NotifierListener<N, E> listener);

	public NotifierListener<N, E> getNotifierListener(int index);

	public void removeNotifierListener(NotifierListener<N, E> listener);

	public void fire(E event);

	public int numListener();
}