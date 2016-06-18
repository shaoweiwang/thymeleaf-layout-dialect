/* 
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.thymeleaf.decorators.html

import nz.net.ultraq.thymeleaf.decorators.Decorator
import nz.net.ultraq.thymeleaf.decorators.SortingStrategy
import nz.net.ultraq.thymeleaf.models.AttributeMerger

import org.thymeleaf.model.IModel
import org.thymeleaf.model.IModelFactory
import org.thymeleaf.model.IOpenElementTag

/**
 * A decorator made to work over an HTML document.  Decoration for a document
 * involves 2 sub-decorators: a special one of the {@code <head>} element, and a
 * standard one for the {@code <body>} element.
 * 
 * @author Emanuel Rabina
 */
class HtmlDocumentDecorator implements Decorator {

	private final IModelFactory modelFactory
	private final SortingStrategy sortingStrategy

	/**
	 * Constructor, apply the given sorting strategy to the decorator.
	 * 
	 * @param modelFactory
	 * @param sortingStrategy
	 */
	HtmlDocumentDecorator(IModelFactory modelFactory, SortingStrategy sortingStrategy) {

		this.modelFactory    = modelFactory
		this.sortingStrategy = sortingStrategy
	}

	/**
	 * Decorate an entire HTML page.
	 * 
	 * @param targetDocumentModel
	 * @param sourceDocumentModel
	 * @return Result of the decoration.
	 */
	@Override
	IModel decorate(IModel targetDocumentModel, IModel sourceDocumentModel) {

		// Head decoration
		def headModelFinder = { event ->
			return event instanceof IOpenElementTag && event.elementCompleteName == 'head'
		}
		def targetHeadModel = targetDocumentModel.findModel(headModelFinder)
		def resultHeadModel = new HtmlHeadDecorator(modelFactory, sortingStrategy).decorate(
			targetHeadModel,
			sourceDocumentModel.findModel(headModelFinder)
		)
		if (resultHeadModel) {
			targetDocumentModel.replaceModel(targetHeadModel.index, resultHeadModel)
		}

		// Body decoration
		def bodyModelFinder = { event ->
			return event instanceof IOpenElementTag && event.elementCompleteName == 'body'
		}
		def targetBodyModel = targetDocumentModel.findModel(bodyModelFinder)
		def resultBodyModel = new HtmlBodyDecorator(modelFactory).decorate(
			targetBodyModel,
			sourceDocumentModel.findModel(bodyModelFinder)
		)
		if (resultBodyModel) {
			targetDocumentModel.replaceModel(targetBodyModel.index, resultBodyModel)
		}

		// TODO
		// Set the doctype from the decorator if missing from the content page
//		def decoratorDocument = decoratorModel.parent
//		def contentDocument   = contentModel.parent
//		if (!contentDocument.docType && decoratorDocument.docType) {
//			contentDocument.docType = decoratorDocument.docType
//		}


		// Find the root element of the target document to merge
		def targetDocumentRootModel = targetDocumentModel.findModel { targetDocumentEvent ->
			return targetDocumentEvent instanceof IOpenElementTag
		}

		// Bring the decorator into the content page (which is the one being processed)
		return new AttributeMerger(modelFactory).merge(targetDocumentRootModel, sourceDocumentModel)
	}
}
