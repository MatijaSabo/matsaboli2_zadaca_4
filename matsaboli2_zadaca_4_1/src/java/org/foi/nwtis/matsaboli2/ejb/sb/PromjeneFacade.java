/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.ejb.sb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.matsaboli2.ejb.eb.Promjene;

/**
 *
 * @author Matija Sabolić
 */
@Stateless
public class PromjeneFacade extends AbstractFacade<Promjene> {

    @PersistenceContext(unitName = "zadaca_4_1PU")
    public EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public PromjeneFacade() {
        super(Promjene.class);
    }
    
}
