package org.faces2.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import at.tfr.faces2.model.Child;

@Named
@FacesConverter(forClass=Child.class)
public class ChildConverter implements Converter {

	@Inject
	private EntityManager entityManager;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		if (value == null || value.trim().length() == 0) {
			return null;
		}
		return entityManager.find(Child.class, Long.valueOf(value));
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		if (value == null || ((Child) value).getId() == null) {
			return "";
		}

		return String.valueOf(((Child) value).getId());
	}

}
