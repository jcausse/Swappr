package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.service.CatalogService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoriesInterceptorTest {

    private static final List<Category> CATEGORIES = List.of(Category.builder().id(1L).name("computers").build());

    @Mock
    private CatalogService catalogService;

    private CategoriesInterceptor interceptor() {
        return new CategoriesInterceptor(catalogService);
    }

    @Test
    public void testPostHandleAddsCategoriesToRenderedView() {
        when(catalogService.getAllCategories()).thenReturn(CATEGORIES);
        final ModelAndView mav = new ModelAndView("landing");

        interceptor().postHandle(null, null, null, mav);

        Assert.assertEquals(CATEGORIES, mav.getModel().get("categories"));
    }

    @Test
    public void testPostHandleKeepsCategoriesAlreadyInModel() {
        final List<Category> fromHandler = List.of(Category.builder().id(2L).name("phones").build());
        final ModelAndView mav = new ModelAndView("listing/discovery").addObject("categories", fromHandler);

        interceptor().postHandle(null, null, null, mav);

        Assert.assertSame(fromHandler, mav.getModel().get("categories"));
    }

    @Test
    public void testPostHandleSkipsRedirectViewName() {
        final ModelAndView mav = new ModelAndView("redirect:/account/listings");

        interceptor().postHandle(null, null, null, mav);

        Assert.assertFalse(mav.getModel().containsKey("categories"));
    }

    @Test
    public void testPostHandleSkipsRedirectViewObject() {
        final ModelAndView mav = new ModelAndView(new RedirectView("/account/listings"));

        interceptor().postHandle(null, null, null, mav);

        Assert.assertFalse(mav.getModel().containsKey("categories"));
    }

    @Test
    public void testPostHandleSkipsNullModelAndView() {
        RuntimeException thrown = null;
        try {
            interceptor().postHandle(null, null, null, null);
        } catch (RuntimeException e) {
            thrown = e;
        }

        Assert.assertNull(thrown);
    }

    @Test
    public void testPostHandleSkipsModelAndViewWithoutView() {
        final ModelAndView mav = new ModelAndView();

        interceptor().postHandle(null, null, null, mav);

        Assert.assertFalse(mav.getModel().containsKey("categories"));
    }
}
