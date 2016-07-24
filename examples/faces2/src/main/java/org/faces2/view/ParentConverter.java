package org.faces2.view;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import at.tfr.faces2.model.Child;
import at.tfr.faces2.model.Parent;

@Named
@FacesConverter(forClass=Parent.class)
public class ParentConverter implements Converter {

	@Inject
	private ParentBean parentBean;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		return parentBean.findById(Long.valueOf(value));
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		if (value == null || ((Parent) value).getId() == null) {
			return "";
		}

		return String.valueOf(((Parent) value).getId());
	}

}
