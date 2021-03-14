package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;

public class RenderResolverEntity implements IRenderResolver {
  public IExpression getParameter(String name) {
    RenderEntityParameterBool renderentityparameterbool = RenderEntityParameterBool.parse(name);
    if (renderentityparameterbool != null)
      return (IExpression)renderentityparameterbool; 
    RenderEntityParameterFloat renderentityparameterfloat = RenderEntityParameterFloat.parse(name);
    return (renderentityparameterfloat != null) ? (IExpression)renderentityparameterfloat : null;
  }
}
