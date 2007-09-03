/*
 * FDRVelocityUpdateStrategy.java
 * 
 * Copyright (C) 2003, 2004 - CIRG@UP 
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science 
 * University of Pretoria
 * South Africa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.cilib.pso.velocityupdatestrategies;

import java.util.Iterator;

import net.sourceforge.cilib.algorithm.Algorithm;
import net.sourceforge.cilib.controlparameterupdatestrategies.ControlParameterUpdateStrategy;
import net.sourceforge.cilib.controlparameterupdatestrategies.LinearDecreasingUpdateStrategy;
import net.sourceforge.cilib.controlparameterupdatestrategies.RandomisedParameterUpdateStrategy;
import net.sourceforge.cilib.entity.Particle;
import net.sourceforge.cilib.entity.Topology;
import net.sourceforge.cilib.problem.Fitness;
import net.sourceforge.cilib.pso.PSO;
import net.sourceforge.cilib.type.types.container.Vector;

/**
 * 
 * @author Olusegun Olorunda
 *
 */
public class FDRVelocityUpdateStrategy extends StandardVelocityUpdate {
	private static final long serialVersionUID = -7117135203986406944L;
	protected ControlParameterUpdateStrategy fdrMaximizerAcceleration;
	
	public FDRVelocityUpdateStrategy() {
		super();
		
		inertiaWeight = new LinearDecreasingUpdateStrategy();
		fdrMaximizerAcceleration = new RandomisedParameterUpdateStrategy();
		
		cognitiveAcceleration.setParameter(1);
        socialAcceleration.setParameter(1);
        fdrMaximizerAcceleration.setParameter(2);
	}
	
	public FDRVelocityUpdateStrategy(FDRVelocityUpdateStrategy copy) {
		this.inertiaWeight = copy.inertiaWeight.clone();
    	this.cognitiveAcceleration = copy.cognitiveAcceleration.clone();
    	this.socialAcceleration = copy.socialAcceleration.clone();
    	this.fdrMaximizerAcceleration = copy.fdrMaximizerAcceleration.clone();
    	this.vMax = copy.vMax.clone();
	}
	
	public FDRVelocityUpdateStrategy clone() {
		return new FDRVelocityUpdateStrategy(this);
	}
	
	public void updateVelocity(Particle particle) {
		Vector velocity = (Vector) particle.getVelocity();
		Vector position = (Vector) particle.getPosition();
		Vector bestPosition = (Vector) particle.getBestPosition();
		Vector neighbourhoodBestPosition = (Vector) particle.getNeighbourhoodBest().getBestPosition();
		
		for (int i = 0; i < particle.getDimension(); ++i) {
			Topology<Particle> topology = ((PSO) Algorithm.get()).getTopology();
			Iterator<Particle> swarmIterator = topology.iterator();
			Particle fdrMaximizer = swarmIterator.next();
			double maxFDR = 0.0;
			
			while (swarmIterator.hasNext()) {
				Particle currentTarget = (Particle) swarmIterator.next();
				
				if (currentTarget.getId().equals(particle.getId())) {
					// do nothing
				} else {
					Fitness currentTargetFitness = currentTarget.getBestFitness();
					Vector currentTargetPosition = (Vector) currentTarget.getBestPosition();
					
					double testFDR = (currentTargetFitness.getValue() - particle.getFitness().getValue())
							/ Math.abs(position.getReal(i) - currentTargetPosition.getReal(i));
					
					if (testFDR > maxFDR) {
						maxFDR = testFDR;
						fdrMaximizer = currentTarget;
					}
				}
			}
			
			Vector fdrMaximizerPosition = (Vector) fdrMaximizer.getBestPosition();
			
			double value = (inertiaWeight.getParameter() * velocity.getReal(i))
						+ cognitiveAcceleration.getParameter() * (bestPosition.getReal(i) - position.getReal(i))
						+ socialAcceleration.getParameter() * (neighbourhoodBestPosition.getReal(i) - position.getReal(i))
						+ fdrMaximizerAcceleration.getParameter() * (fdrMaximizerPosition.getReal(i) - position.getReal(i));
			
			velocity.setReal(i, value);
			clamp(velocity, i);
		}
	}
	
	public void updateControlParameters() {
		inertiaWeight.updateParameter();
		cognitiveAcceleration.updateParameter();
		socialAcceleration.updateParameter();
		fdrMaximizerAcceleration.updateParameter();
		vMax.updateParameter();
	}
	
	/**
	 * @return the fdrMaximizerAcceleration
	 */
	public ControlParameterUpdateStrategy getFdrMaximizerAcceleration() {
		return fdrMaximizerAcceleration;
	}
	
	/**
	 * @param fdrMaximizerAcceleration
	 *            the fdrMaximizerAcceleration to set
	 */
	public void setFdrMaximizerAcceleration(ControlParameterUpdateStrategy fdrMaximizerAcceleration) {
		this.fdrMaximizerAcceleration = fdrMaximizerAcceleration;
	}
	
}